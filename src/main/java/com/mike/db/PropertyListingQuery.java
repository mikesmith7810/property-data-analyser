package com.mike.db;

public record PropertyListingQuery(
        String agentName,
        String town,
        boolean reducedOnly,
        boolean newHomeOnly,
        String sortBy,
        int page,
        int size
) {}
