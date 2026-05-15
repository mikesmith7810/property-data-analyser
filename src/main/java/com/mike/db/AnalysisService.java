package com.mike.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class AnalysisService {

    @Inject
    EntityManager em;

    @SuppressWarnings("unchecked")
    public List<AvgPriceByBedrooms> avgPriceByBedrooms(String town) {
        boolean filterTown = town != null && !town.isBlank();
        String where = "WHERE p.price > 0 AND p.bedrooms BETWEEN 1 AND 6"
                + (filterTown ? " AND LOWER(p.displayAddress) LIKE :town" : "");

        var query = em.createQuery(
                "SELECT p.bedrooms, AVG(p.price), COUNT(p) FROM PropertyListing p "
                + where
                + " GROUP BY p.bedrooms ORDER BY p.bedrooms");

        if (filterTown) {
            query.setParameter("town", "%" + town.toLowerCase() + "%");
        }

        return ((List<Object[]>) query.getResultList()).stream()
                .map(r -> new AvgPriceByBedrooms(
                        ((Number) r[0]).intValue(),
                        Math.round((Double) r[1]),
                        (Long) r[2]))
                .toList();
    }
}
