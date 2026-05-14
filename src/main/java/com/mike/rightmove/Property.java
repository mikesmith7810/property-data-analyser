package com.mike.rightmove;

import java.util.List;

public record Property(
        long id,
        int bedrooms,
        int bathrooms,
        int numberOfImages,
        int numberOfFloorplans,
        String summary,
        String displayAddress,
        double latitude,
        double longitude,
        int price,
        String priceDisplay,
        String priceQualifier,
        String propertySubType,
        String propertyTypeFullDescription,
        String tenureType,
        String displaySize,
        String branchName,
        String contactTelephone,
        String contactUrl,
        String addedOrReduced,
        String firstVisibleDate,
        String propertyUrl,
        String mainImageUrl,
        List<String> imageUrls,
        List<String> keyFeatures,
        boolean featuredProperty,
        boolean auction
) {}
