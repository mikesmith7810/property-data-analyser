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
        Long count = em.createQuery("SELECT COUNT(s) FROM SyncLocation s", Long.class)
                .getSingleResult();
        if (count > 0) return;

        List.of(
                new String[]{"Bridport",    "214",  "REGION"},
                new String[]{"Weymouth",    "1440", "REGION"},
                new String[]{"Dorchester",  "431",  "REGION"},
                new String[]{"Portland",    "1086", "REGION"}
        ).forEach(row -> {
            SyncLocation loc = new SyncLocation();
            loc.name       = row[0];
            loc.locationId = row[1];
            loc.locationType = row[2];
            em.persist(loc);
        });
    }
}
