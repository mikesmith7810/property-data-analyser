package com.mike.db;

import com.mike.rightmove.Property;
import com.mike.rightmove.PropertyDetail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class PropertyListingRepository {

    @Inject
    EntityManager em;

    @Transactional
    public void persistFromSearch(Property search) {
        PropertyListing listing = em.find(PropertyListing.class, search.id());
        if (listing == null) {
            listing = new PropertyListing();
            listing.id = search.id();
        }
        listing.bedrooms = search.bedrooms();
        listing.bathrooms = search.bathrooms();
        listing.displayAddress = search.displayAddress();
        listing.latitude = search.latitude();
        listing.longitude = search.longitude();
        listing.price = search.price();
        listing.priceDisplay = search.priceDisplay();
        listing.priceQualifier = search.priceQualifier();
        listing.propertySubType = search.propertySubType();
        listing.propertyTypeFullDescription = search.propertyTypeFullDescription();
        listing.tenureType = search.tenureType();
        listing.displaySize = search.displaySize();
        listing.branchName = search.branchName();
        listing.contactTelephone = search.contactTelephone();
        listing.contactUrl = search.contactUrl();
        listing.addedOrReduced = search.addedOrReduced();
        listing.firstVisibleDate = search.firstVisibleDate();
        listing.propertyUrl = search.propertyUrl();
        listing.mainImageUrl = search.mainImageUrl();
        listing.imageUrls = search.imageUrls();
        listing.keyFeatures = search.keyFeatures();
        listing.numberOfImages = search.numberOfImages();
        listing.numberOfFloorplans = search.numberOfFloorplans();
        listing.featuredProperty = search.featuredProperty();
        listing.auction = search.auction();
        listing.summary = search.summary();
        listing.detailFetched = false;
        em.merge(listing);
    }

    @Transactional
    public void enrichWithDetail(long propertyId, PropertyDetail detail) {
        PropertyListing listing = em.find(PropertyListing.class, propertyId);
        if (listing == null) return;
        listing.description = detail.description();
        listing.outcode = detail.outcode();
        listing.ukCountry = detail.ukCountry();
        listing.yearsRemainingOnLease = detail.yearsRemainingOnLease();
        listing.floorplanUrls = detail.floorplanUrls();
        listing.nearestStations = detail.nearestStations().stream()
                .map(s -> new NearestStationData(s.name(), s.distance(), s.unit()))
                .toList();
        listing.listingUpdateReason = detail.listingUpdateReason();
        listing.published = detail.published();
        listing.preOwned = detail.preOwned();
        listing.addedDate = detail.addedDate();
        listing.soldSTC = detail.soldSTC();
        listing.propertyPhrase = detail.propertyPhrase();
        listing.pageTitle = detail.pageTitle();
        listing.detailFetched = true;
        em.merge(listing);
    }
}
