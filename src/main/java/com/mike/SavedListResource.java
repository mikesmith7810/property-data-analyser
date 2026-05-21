package com.mike;

import com.mike.db.PropertyListing;
import com.mike.db.PropertyListingService;
import com.mike.db.SavedList;
import com.mike.db.SavedListService;
import com.mike.db.SavedListSummary;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/saved-lists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SavedListResource {

    @Inject
    SavedListService savedListService;

    @Inject
    PropertyListingService propertyListingService;

    @GET
    @Blocking
    public List<SavedListSummary> getAllLists() {
        return savedListService.findAllLists().stream()
                .map(l -> new SavedListSummary(
                        l.id,
                        l.name,
                        l.createdAt != null ? l.createdAt.toString() : null,
                        savedListService.countEntriesForList(l.id)))
                .toList();
    }

    @POST
    @Blocking
    public Response createList(Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        SavedList created = savedListService.createList(name.trim());
        SavedListSummary summary = new SavedListSummary(
                created.id,
                created.name,
                created.createdAt != null ? created.createdAt.toString() : null,
                0L);
        return Response.status(Response.Status.CREATED).entity(summary).build();
    }

    @DELETE
    @Path("/{listId}")
    @Blocking
    public Response deleteList(@PathParam("listId") Long listId) {
        boolean deleted = savedListService.deleteList(listId);
        if (!deleted) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.noContent().build();
    }

    @GET
    @Path("/{listId}/entries")
    @Blocking
    public Response getListEntries(@PathParam("listId") Long listId) {
        List<Long> ids = savedListService.findEntriesForList(listId).stream()
                .map(e -> e.propertyId)
                .toList();
        List<PropertyListing> listings = propertyListingService.findByIds(ids);
        return Response.ok(listings).build();
    }

    @GET
    @Path("/{listId}/property-ids")
    @Blocking
    public Set<Long> getPropertyIds(@PathParam("listId") Long listId) {
        return savedListService.findPropertyIdsInList(listId);
    }

    @POST
    @Path("/{listId}/entries")
    @Blocking
    public Response addEntry(@PathParam("listId") Long listId, Map<String, Object> body) {
        Object raw = body.get("propertyId");
        if (raw == null) return Response.status(Response.Status.BAD_REQUEST).build();
        Long propertyId = ((Number) raw).longValue();
        savedListService.addEntry(listId, propertyId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{listId}/entries/{propertyId}")
    @Blocking
    public Response removeEntry(@PathParam("listId") Long listId, @PathParam("propertyId") Long propertyId) {
        savedListService.removeEntry(listId, propertyId);
        return Response.noContent().build();
    }
}
