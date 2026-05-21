package com.mike.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class AnalysisService {

    // Must have at least this many raw properties to attempt analysis
    private static final int MIN_RAW = 3;
    // Must have at least this many remaining after outlier removal to show a result
    private static final int MIN_FILTERED = 2;

    @Inject
    EntityManager em;

    @SuppressWarnings("unchecked")
    public List<AvgPriceByBedrooms> avgPriceByBedrooms(String town, boolean newHomeOnly) {
        boolean filterTown = town != null && !town.isBlank();

        String locationClause;
        List<String> locationNames = List.of();

        if (filterTown) {
            locationClause = " AND LOWER(p.displayAddress) LIKE :town";
        } else {
            locationNames = em.createQuery("SELECT s.name FROM SyncLocation s", String.class)
                    .getResultList();
            if (locationNames.isEmpty()) {
                locationClause = "";
            } else {
                String orClauses = IntStream.range(0, locationNames.size())
                        .mapToObj(i -> "LOWER(p.displayAddress) LIKE :loc" + i)
                        .reduce((a, b) -> a + " OR " + b)
                        .orElse("1=1");
                locationClause = " AND (" + orClauses + ")";
            }
        }

        String where = "WHERE p.price > 0 AND p.bedrooms BETWEEN 1 AND 6"
                + locationClause
                + (newHomeOnly ? " AND p.preOwned = 'New Home'" : "");

        var query = em.createQuery(
                "SELECT p.bedrooms, p.price FROM PropertyListing p "
                + where
                + " ORDER BY p.bedrooms, p.price");

        if (filterTown) {
            query.setParameter("town", "%" + town.toLowerCase() + "%");
        } else {
            for (int i = 0; i < locationNames.size(); i++) {
                query.setParameter("loc" + i, "%" + locationNames.get(i).toLowerCase() + "%");
            }
        }

        Map<Integer, List<Long>> pricesByBeds = ((List<Object[]>) query.getResultList()).stream()
                .collect(Collectors.groupingBy(
                        r -> ((Number) r[0]).intValue(),
                        Collectors.mapping(r -> ((Number) r[1]).longValue(), Collectors.toList())));

        return pricesByBeds.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(e -> e.getValue().size() >= MIN_RAW)
                .map(e -> {
                    List<Long> filtered = removeMadOutliers(e.getValue());
                    return new AvgPriceByBedrooms(e.getKey(), median(filtered), (long) filtered.size());
                })
                .filter(r -> r.count() >= MIN_FILTERED)
                .toList();
    }

    /**
     * Removes outliers using Median Absolute Deviation (MAD).
     * Excludes any price more than 3 × MAD from the group median.
     * Works in both directions — catches suspiciously low data errors
     * as well as high luxury outliers.
     * Requires a sorted list.
     */
    private List<Long> removeMadOutliers(List<Long> sorted) {
        long med = median(sorted);
        List<Long> deviations = sorted.stream()
                .map(p -> Math.abs(p - med))
                .sorted()
                .toList();
        long mad = median(deviations);
        if (mad == 0) return sorted; // all identical prices, nothing to remove
        long threshold = mad * 3;
        return sorted.stream()
                .filter(p -> Math.abs(p - med) <= threshold)
                .collect(Collectors.toList());
    }

    private long median(List<Long> sorted) {
        int n = sorted.size();
        return n % 2 == 1
                ? sorted.get(n / 2)
                : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2;
    }
}
