package com.mike.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class PropertyListingService {

    private static final Pattern POSTCODE = Pattern.compile(
            "^[A-Z]{1,2}[0-9][0-9A-Z]?(\\s+[0-9][A-Z]{2})?$");
    private static final Set<String> EXCLUDED = Set.of(
            "Dorset", "Devon", "Somerset", "Hampshire", "Wiltshire",
            "England", "Wales", "Scotland", "United Kingdom");
    private static final Set<String> STREET_SUFFIXES = Set.of(
            "street", "road", "lane", "avenue", "close", "drive", "way",
            "terrace", "place", "court", "gardens", "garden", "grove", "rise",
            "row", "square", "walk", "hill", "view", "park", "quay", "crescent",
            "mews", "yard", "path", "passage", "broadway", "approach");

    @Inject
    EntityManager em;

    public PropertyListingPage queryListings(PropertyListingQuery query) {
        List<String> conditions = new ArrayList<>();
        if (query.agentName() != null && !query.agentName().isBlank()) {
            conditions.add("LOWER(p.branchName) LIKE :agentName");
        }
        if (query.town() != null && !query.town().isBlank()) {
            conditions.add("LOWER(p.displayAddress) LIKE :town");
        }
        if (query.reducedOnly()) {
            conditions.add("LOWER(p.addedOrReduced) LIKE 'reduced%'");
        }
        if (query.newHomeOnly()) {
            conditions.add("p.preOwned = 'New Home'");
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
        if (query.town() != null && !query.town().isBlank()) {
            String param = "%" + query.town().toLowerCase() + "%";
            listQ.setParameter("town", param);
            countQ.setParameter("town", param);
        }

        int size = query.size() > 0 ? query.size() : 20;
        listQ.setFirstResult(query.page() * size);
        listQ.setMaxResults(size);

        return new PropertyListingPage(listQ.getResultList(), countQ.getSingleResult(), query.page(), size);
    }

    public PropertyListing findById(long id) {
        return em.find(PropertyListing.class, id);
    }

    public List<String> findTowns() {
        return em.createQuery(
                        "SELECT p.displayAddress FROM PropertyListing p WHERE p.displayAddress IS NOT NULL",
                        String.class)
                .getResultList()
                .stream()
                .flatMap(addr -> Arrays.stream(addr.split("[,\r\n]+")))
                .map(String::trim)
                .filter(s -> s.length() > 3)
                .filter(s -> !POSTCODE.matcher(s).matches())
                .filter(s -> !EXCLUDED.contains(s))
                .filter(s -> Character.isLetter(s.charAt(0)))
                .filter(s -> {
                    String lastWord = s.contains(" ")
                            ? s.substring(s.lastIndexOf(' ') + 1).toLowerCase()
                            : s.toLowerCase();
                    return !STREET_SUFFIXES.contains(lastWord);
                })
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() >= 5)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<AgentCount> findAgents() {
        return em.createQuery(
                "SELECT p.branchName, COUNT(p) FROM PropertyListing p" +
                " WHERE p.branchName IS NOT NULL AND p.branchName <> ''" +
                " GROUP BY p.branchName ORDER BY COUNT(p) DESC")
                .getResultList()
                .stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new AgentCount((String) r[0], (Long) r[1]);
                })
                .toList();
    }
}
