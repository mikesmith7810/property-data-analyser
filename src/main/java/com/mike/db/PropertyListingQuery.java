package com.mike.db;

public record PropertyListingQuery(
        String agentName,
        String town,
        boolean reducedOnly,
        boolean newHomeOnly,
        boolean vacantOnly,
        int beds,
        String sortBy,
        int page,
        int size
) {}
