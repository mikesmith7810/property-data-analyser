package com.mike.db;

import java.util.List;

public record PropertyListingPage(
        List<PropertyListing> listings,
        long totalCount,
        int page,
        int size
) {}
