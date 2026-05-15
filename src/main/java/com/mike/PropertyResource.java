package com.mike;

import com.mike.db.AgentCount;
import com.mike.db.AnalysisService;
import com.mike.db.AvgPriceByBedrooms;
import com.mike.db.SyncLocation;
import com.mike.db.PropertyListingPage;
import com.mike.db.PropertyListing;
import com.mike.db.PropertyListingQuery;
import com.mike.db.PropertyListingService;
import com.mike.db.PropertySyncService;
import com.mike.db.SyncResult;
import com.mike.rightmove.Location;
import com.mike.rightmove.LocationService;
import com.mike.rightmove.Property;
import com.mike.rightmove.PropertyDetail;
import com.mike.rightmove.PropertyDetailService;
import com.mike.rightmove.PropertySearchService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/property")
public class PropertyResource {

    @Inject
    LocationService locationService;

    @Inject
    PropertySearchService propertySearchService;

    @Inject
    PropertyDetailService propertyDetailService;

    @Inject
    PropertySyncService propertySyncService;

    @Inject
    PropertyListingService propertyListingService;

    @Inject
    EntityManager em;

    @Inject
    AnalysisService analysisService;

    @GET
    @Path("/locations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Location> getLocations(@QueryParam("query") String query) throws Exception {
        return locationService.searchLocations(query, 10);
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Property> searchProperties(
            @QueryParam("locationId") String locationId,
            @QueryParam("locationType") String locationType,
            @QueryParam("radius") Double radius,
            @QueryParam("propertyTypes") String propertyTypes,
            @QueryParam("minPrice") Integer minPrice,
            @QueryParam("maxPrice") Integer maxPrice,
            @QueryParam("minBedrooms") Integer minBedrooms,
            @QueryParam("maxBedrooms") Integer maxBedrooms
    ) throws Exception {
        return propertySearchService.searchProperties(
                locationId, locationType, radius, propertyTypes, minPrice, maxPrice, minBedrooms, maxBedrooms
        );
    }

    @GET
    @Path("/analysis/avg-price-by-bedrooms")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public List<AvgPriceByBedrooms> avgPriceByBedrooms(@QueryParam("town") String town) {
        return analysisService.avgPriceByBedrooms(town);
    }

    @GET
    @Path("/sync-locations")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public List<SyncLocation> getSyncLocations() {
        return em.createQuery("SELECT s FROM SyncLocation s ORDER BY s.name", SyncLocation.class)
                .getResultList();
    }

    @GET
    @Path("/agents")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public List<AgentCount> getAgents() {
        return propertyListingService.findAgents();
    }

    @GET
    @Path("/towns")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public List<String> getTowns() {
        return propertyListingService.findTowns();
    }

    @GET
    @Path("/listings")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public PropertyListingPage getListings(
            @QueryParam("agentName") String agentName,
            @QueryParam("town") String town,
            @QueryParam("reduced") @DefaultValue("false") boolean reducedOnly,
            @QueryParam("newHome") @DefaultValue("false") boolean newHomeOnly,
            @QueryParam("sortBy") @DefaultValue("daysOnMarket") String sortBy,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size
    ) {
        return propertyListingService.queryListings(
                new PropertyListingQuery(agentName, town, reducedOnly, newHomeOnly, sortBy, page, size));
    }

    @GET
    @Path("/listings/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public Response getListingById(@PathParam("id") long id) {
        PropertyListing listing = propertyListingService.findById(id);
        if (listing == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(listing).build();
    }

    @GET
    @Path("/{propertyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PropertyDetail getPropertyDetail(@PathParam("propertyId") long propertyId) throws Exception {
        return propertyDetailService.getPropertyDetail(propertyId);
    }

    @GET
    @Path("/sync")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public SyncResult syncProperties(
            @QueryParam("locationId") String locationId,
            @QueryParam("locationType") String locationType,
            @QueryParam("radius") Double radius,
            @QueryParam("propertyTypes") String propertyTypes,
            @QueryParam("minPrice") Integer minPrice,
            @QueryParam("maxPrice") Integer maxPrice,
            @QueryParam("minBedrooms") Integer minBedrooms,
            @QueryParam("maxBedrooms") Integer maxBedrooms
    ) throws Exception {
        SyncResult result = propertySyncService.syncProperties(
                locationId, locationType, radius, propertyTypes, minPrice, maxPrice, minBedrooms, maxBedrooms);
        stampLastSynced(locationId);
        return result;
    }

    @Transactional
    void stampLastSynced(String locationId) {
        em.createQuery("SELECT s FROM SyncLocation s WHERE s.locationId = :lid", SyncLocation.class)
                .setParameter("lid", locationId)
                .getResultStream()
                .findFirst()
                .ifPresent(loc -> loc.lastSyncedAt = LocalDateTime.now());
    }
}
