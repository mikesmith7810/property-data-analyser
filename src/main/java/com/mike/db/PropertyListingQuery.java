package com.mike.db;

public record PropertyListingQuery(
        String agentName,
        boolean reducedOnly,
        boolean newHomeOnly,
        String sortBy,
        int page,
        int size
) {}
