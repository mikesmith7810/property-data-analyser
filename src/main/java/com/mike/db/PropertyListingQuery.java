package com.mike.db;

public record PropertyListingQuery(
        String agentName,
        boolean reducedOnly,
        String sortBy,
        int page,
        int size
) {}
