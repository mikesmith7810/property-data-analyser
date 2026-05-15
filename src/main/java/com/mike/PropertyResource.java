package com.mike;

import com.mike.db.PropertySyncService;
import com.mike.db.SyncResult;
import com.mike.rightmove.Location;
import com.mike.rightmove.LocationService;
import com.mike.rightmove.Property;
import com.mike.rightmove.PropertyDetail;
import com.mike.rightmove.PropertyDetailService;
import com.mike.rightmove.PropertySearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
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
    @Path("/{propertyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PropertyDetail getPropertyDetail(@PathParam("propertyId") long propertyId) throws Exception {
        return propertyDetailService.getPropertyDetail(propertyId);
    }

    @GET
    @Path("/sync")
    @Produces(MediaType.APPLICATION_JSON)
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
        return propertySyncService.syncProperties(
                locationId, locationType, radius, propertyTypes, minPrice, maxPrice, minBedrooms, maxBedrooms
        );
    }
}
