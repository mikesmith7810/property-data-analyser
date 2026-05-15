package com.mike.db;

import com.mike.rightmove.Property;
import com.mike.rightmove.PropertyDetailService;
import com.mike.rightmove.PropertySearchService;
import com.mike.rightmove.SearchPage;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class PropertySyncService {

    private static final int PAGE_SIZE = 24;
    private static final int DETAIL_CONCURRENCY = 5;
    private static final int JITTER_MIN_MS = 200;
    private static final int JITTER_RANGE_MS = 300;

    @Inject
    PropertySearchService propertySearchService;

    @Inject
    PropertyDetailService propertyDetailService;

    @Inject
    PropertyListingRepository repository;

    public SyncResult syncProperties(
            String locationId,
            String locationType,
            Double radius,
            String propertyTypes,
            Integer minPrice,
            Integer maxPrice,
            Integer minBedrooms,
            Integer maxBedrooms
    ) throws Exception {

        // Phase 1: paginate all search results and persist basic data
        List<Property> allProperties = fetchAllSearchPages(
                locationId, locationType, radius, propertyTypes, minPrice, maxPrice, minBedrooms, maxBedrooms
        );
        Log.infof("Phase 1 complete: found %d properties across search pages", allProperties.size());

        for (Property p : allProperties) {
            try {
                repository.persistFromSearch(p);
            } catch (Exception e) {
                Log.warnf("Failed to persist search result for property %d: %s", p.id(), e.getMessage());
            }
        }

        // Phase 2: parallel detail fetching with bounded concurrency and jitter
        AtomicInteger enriched = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        int total = allProperties.size();

        Log.infof("Phase 2 starting: fetching detail for %d properties (concurrency=%d)", total, DETAIL_CONCURRENCY);

        Semaphore semaphore = new Semaphore(DETAIL_CONCURRENCY);
        Random jitter = new Random();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        List<CompletableFuture<Void>> futures = allProperties.stream()
                .map(p -> CompletableFuture.runAsync(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            Thread.sleep(JITTER_MIN_MS + jitter.nextInt(JITTER_RANGE_MS));
                            var detail = propertyDetailService.getPropertyDetail(p.id());
                            repository.enrichWithDetail(p.id(), detail);
                            int done = enriched.incrementAndGet();
                            Log.infof("[%d/%d] Enriched property %d - %s", done, total, p.id(), p.displayAddress());
                        } finally {
                            semaphore.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        failed.incrementAndGet();
                    } catch (Exception e) {
                        Log.warnf("Failed to enrich property %d: %s", p.id(), e.getMessage());
                        failed.incrementAndGet();
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.close();

        Log.infof("Phase 2 complete: enriched=%d failed=%d", enriched.get(), failed.get());
        return new SyncResult(allProperties.size(), enriched.get(), failed.get());
    }

    private List<Property> fetchAllSearchPages(
            String locationId,
            String locationType,
            Double radius,
            String propertyTypes,
            Integer minPrice,
            Integer maxPrice,
            Integer minBedrooms,
            Integer maxBedrooms
    ) throws Exception {
        // Use a map keyed by ID to deduplicate featured properties that appear on every page
        Map<Long, Property> unique = new LinkedHashMap<>();
        int index = 0;

        while (true) {
            SearchPage page = propertySearchService.searchPage(
                    locationId, locationType, radius, propertyTypes,
                    minPrice, maxPrice, minBedrooms, maxBedrooms, index
            );

            if (page.properties().isEmpty()) break;
            page.properties().forEach(p -> unique.put(p.id(), p));

            Log.infof("Fetched search page at index=%d: %d properties (%d unique so far, %d total available)",
                    index, page.properties().size(), unique.size(), page.totalResultCount());

            // Stop if Rightmove says we have everything, or the page was short (last page)
            if (unique.size() >= page.totalResultCount() || page.properties().size() < PAGE_SIZE) break;

            index += PAGE_SIZE;
            Thread.sleep(500);
        }

        return new ArrayList<>(unique.values());
    }
}
