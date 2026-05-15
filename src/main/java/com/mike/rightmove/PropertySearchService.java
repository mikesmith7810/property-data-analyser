package com.mike.rightmove;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PropertySearchService {

    @Inject
    RightmoveClient rightmoveClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Property> searchProperties(
            String locationId,
            String locationType,
            Double radius,
            String propertyTypes,
            Integer minPrice,
            Integer maxPrice,
            Integer minBedrooms,
            Integer maxBedrooms
    ) throws Exception {
        return searchPage(locationId, locationType, radius, propertyTypes,
                minPrice, maxPrice, minBedrooms, maxBedrooms, 0).properties();
    }

    public SearchPage searchPage(
            String locationId,
            String locationType,
            Double radius,
            String propertyTypes,
            Integer minPrice,
            Integer maxPrice,
            Integer minBedrooms,
            Integer maxBedrooms,
            int index
    ) throws Exception {
        String locationIdentifier = URLEncoder.encode(locationType + "^" + locationId, StandardCharsets.UTF_8);
        StringBuilder url = new StringBuilder("https://www.rightmove.co.uk/property-for-sale/find.html?locationIdentifier=")
                .append(locationIdentifier)
                .append("&index=").append(index);

        if (radius != null) url.append("&radius=").append(radius);
        if (propertyTypes != null && !propertyTypes.isBlank()) url.append("&propertyTypes=").append(URLEncoder.encode(propertyTypes, StandardCharsets.UTF_8));
        if (minPrice != null) url.append("&minPrice=").append(minPrice);
        if (maxPrice != null) url.append("&maxPrice=").append(maxPrice);
        if (minBedrooms != null) url.append("&minBedrooms=").append(minBedrooms);
        if (maxBedrooms != null) url.append("&maxBedrooms=").append(maxBedrooms);

        String html = rightmoveClient.get(url.toString());
        String json = extractNextData(html);

        JsonNode root = objectMapper.readTree(json);
        JsonNode searchResults = root.path("props").path("pageProps").path("searchResults");
        int totalResultCount = searchResults.path("resultCount").asInt(0);
        JsonNode properties = searchResults.path("properties");

        List<Property> results = new ArrayList<>();
        for (JsonNode node : properties) {
            JsonNode loc = node.path("location");
            JsonNode priceNode = node.path("price");
            JsonNode displayPrices = priceNode.path("displayPrices");
            String priceDisplay = displayPrices.isArray() && displayPrices.size() > 0
                    ? displayPrices.get(0).path("displayPrice").asText("") : "";
            String priceQualifier = displayPrices.isArray() && displayPrices.size() > 0
                    ? displayPrices.get(0).path("displayPriceQualifier").asText("") : "";

            JsonNode customer = node.path("customer");
            JsonNode propertyImages = node.path("propertyImages");

            List<String> imageUrls = new ArrayList<>();
            for (JsonNode img : propertyImages.path("images")) {
                imageUrls.add(img.path("srcUrl").asText(""));
            }

            List<String> keyFeatures = new ArrayList<>();
            for (JsonNode kf : node.path("keyFeatures")) {
                keyFeatures.add(kf.path("description").asText(""));
            }

            results.add(new Property(
                    node.path("id").asLong(),
                    node.path("bedrooms").asInt(),
                    node.path("bathrooms").asInt(),
                    node.path("numberOfImages").asInt(),
                    node.path("numberOfFloorplans").asInt(),
                    node.path("summary").asText(""),
                    node.path("displayAddress").asText(""),
                    loc.path("latitude").asDouble(),
                    loc.path("longitude").asDouble(),
                    priceNode.path("amount").asInt(),
                    priceDisplay,
                    priceQualifier,
                    node.path("propertySubType").asText(""),
                    node.path("propertyTypeFullDescription").asText(""),
                    node.path("tenure").path("tenureType").asText(""),
                    node.path("displaySize").asText(""),
                    customer.path("branchDisplayName").asText(""),
                    customer.path("contactTelephone").asText(""),
                    node.path("contactUrl").asText(""),
                    node.path("addedOrReduced").asText(""),
                    node.path("firstVisibleDate").asText(""),
                    node.path("propertyUrl").asText(""),
                    propertyImages.path("mainImageSrc").asText(""),
                    imageUrls,
                    keyFeatures,
                    node.path("featuredProperty").asBoolean(false),
                    node.path("auction").asBoolean(false)
            ));
        }
        return new SearchPage(results, totalResultCount);
    }

    private String extractNextData(String html) {
        String marker = "<script id=\"__NEXT_DATA__\" type=\"application/json\">";
        int start = html.indexOf(marker);
        if (start == -1) {
            throw new IllegalStateException("__NEXT_DATA__ not found in Rightmove response");
        }
        start += marker.length();
        int end = html.indexOf("</script>", start);
        return html.substring(start, end);
    }
}
