package com.mike.rightmove;

import java.util.List;

public record PropertyDetail(
        long id,
        int bedrooms,
        int bathrooms,
        String displayAddress,
        String outcode,
        String ukCountry,
        String primaryPrice,
        String priceQualifier,
        double latitude,
        double longitude,
        String description,
        String propertyPhrase,
        String pageTitle,
        String propertySubType,
        String tenureType,
        int yearsRemainingOnLease,
        String branchDisplayName,
        String contactTelephone,
        List<String> keyFeatures,
        List<String> imageUrls,
        List<String> floorplanUrls,
        List<NearestStation> nearestStations,
        String listingUpdateReason,
        boolean published
) {
    public record NearestStation(String name, double distance, String unit) {}
}
