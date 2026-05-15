package com.mike.db;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "property_listings")
public class PropertyListing extends PanacheEntityBase {

    @Id
    public long id;

    // --- Core ---
    public int bedrooms;
    public int bathrooms;
    public String propertySubType;
    public String propertyTypeFullDescription;
    public String tenureType;
    public int yearsRemainingOnLease;
    public String displaySize;
    public boolean auction;
    public boolean featuredProperty;
    public boolean soldSTC;
    public boolean published;

    // --- new build / ownership ---
    public String preOwned;

    // --- Address ---
    public String displayAddress;
    public String outcode;
    public String ukCountry;

    // --- Price ---
    public int price;
    public String priceDisplay;
    public String priceQualifier;

    // --- Location ---
    public double latitude;
    public double longitude;

    // --- Text ---
    @Column(length = 2000)
    public String summary;

    @Column(columnDefinition = "text")
    public String description;

    public String propertyPhrase;
    public String pageTitle;

    // --- Dates / history ---
    public String addedDate;
    public String firstVisibleDate;
    public String addedOrReduced;
    public String listingUpdateReason;

    // --- Agent ---
    public String branchName;
    public String contactTelephone;
    public String contactUrl;
    public String propertyUrl;

    // --- Images ---
    public String mainImageUrl;
    public int numberOfImages;
    public int numberOfFloorplans;

    // --- Array fields stored as JSON ---
    @Column(columnDefinition = "json")
    @Convert(converter = StringListConverter.class)
    public List<String> imageUrls;

    @Column(columnDefinition = "json")
    @Convert(converter = StringListConverter.class)
    public List<String> keyFeatures;

    @Column(columnDefinition = "json")
    @Convert(converter = StringListConverter.class)
    public List<String> floorplanUrls;

    @Column(columnDefinition = "json")
    @Convert(converter = NearestStationConverter.class)
    public List<NearestStationData> nearestStations;

    // --- Sync state ---
    public boolean detailFetched;

    // --- Record keeping ---
    @Column(updatable = false)
    public LocalDateTime createdAt;

    public LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
