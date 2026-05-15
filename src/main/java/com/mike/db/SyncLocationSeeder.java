package com.mike.db;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class SyncLocationSeeder {

    @Inject
    EntityManager em;

    void onStart(@Observes StartupEvent ev) {
        seed();
    }

    @Transactional
    void seed() {
        List.of(
                new String[]{"Bridport",   "214",  "REGION"},
                new String[]{"Weymouth",   "1440", "REGION"},
                new String[]{"Dorchester", "431",  "REGION"},
                new String[]{"Portland",   "1086", "REGION"},
                new String[]{"Swanage",    "1302", "REGION"}
        ).forEach(row -> {
            long exists = em.createQuery(
                            "SELECT COUNT(s) FROM SyncLocation s WHERE s.locationId = :lid", Long.class)
                    .setParameter("lid", row[1])
                    .getSingleResult();
            if (exists == 0) {
                SyncLocation loc = new SyncLocation();
                loc.name         = row[0];
                loc.locationId   = row[1];
                loc.locationType = row[2];
                em.persist(loc);
            }
        });
    }
}
