package com.mike.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PropertyListingService {

    @Inject
    EntityManager em;

    public PropertyListingPage queryListings(PropertyListingQuery query) {
        List<String> conditions = new ArrayList<>();
        if (query.agentName() != null && !query.agentName().isBlank()) {
            conditions.add("LOWER(p.branchName) LIKE :agentName");
        }
        if (query.reducedOnly()) {
            conditions.add("p.addedOrReduced = 'Reduced'");
        }

        String where = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
        String order = switch (query.sortBy() == null ? "" : query.sortBy()) {
            case "priceAsc"  -> "p.price ASC";
            case "priceDesc" -> "p.price DESC";
            default          -> "p.firstVisibleDate ASC";
        };

        TypedQuery<PropertyListing> listQ = em.createQuery(
                "SELECT p FROM PropertyListing p " + where + " ORDER BY " + order, PropertyListing.class);
        TypedQuery<Long> countQ = em.createQuery(
                "SELECT COUNT(p) FROM PropertyListing p " + where, Long.class);

        if (query.agentName() != null && !query.agentName().isBlank()) {
            String param = "%" + query.agentName().toLowerCase() + "%";
            listQ.setParameter("agentName", param);
            countQ.setParameter("agentName", param);
        }

        int size = query.size() > 0 ? query.size() : 20;
        listQ.setFirstResult(query.page() * size);
        listQ.setMaxResults(size);

        return new PropertyListingPage(listQ.getResultList(), countQ.getSingleResult(), query.page(), size);
    }

    public PropertyListing findById(long id) {
        return em.find(PropertyListing.class, id);
    }
}
