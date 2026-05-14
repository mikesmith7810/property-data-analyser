package com.mike.rightmove;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PropertyDetailService {

    @Inject
    RightmoveClient rightmoveClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PropertyDetail getPropertyDetail(long propertyId) throws Exception {
        String html = rightmoveClient.get("https://www.rightmove.co.uk/properties/" + propertyId);
        JsonNode[] nodes = parsePageModelNodes(html);
        JsonNode prop = resolveNode(nodes, nodes[1]);

        JsonNode prices = prop.path("prices");
        JsonNode address = prop.path("address");
        JsonNode location = prop.path("location");
        JsonNode text = prop.path("text");
        JsonNode tenure = prop.path("tenure");
        JsonNode customer = prop.path("customer");
        JsonNode contactInfo = prop.path("contactInfo");
        JsonNode status = prop.path("status");
        JsonNode listingHistory = prop.path("listingHistory");

        List<String> keyFeatures = new ArrayList<>();
        for (JsonNode kf : prop.path("keyFeatures")) {
            keyFeatures.add(kf.asText(""));
        }

        List<String> imageUrls = new ArrayList<>();
        for (JsonNode img : prop.path("images")) {
            imageUrls.add(img.path("url").asText(""));
        }

        List<String> floorplanUrls = new ArrayList<>();
        for (JsonNode fp : prop.path("floorplans")) {
            floorplanUrls.add(fp.path("url").asText(""));
        }

        List<PropertyDetail.NearestStation> stations = new ArrayList<>();
        for (JsonNode station : prop.path("nearestStations")) {
            stations.add(new PropertyDetail.NearestStation(
                    station.path("name").asText(""),
                    station.path("distance").asDouble(),
                    station.path("unit").asText("")
            ));
        }

        return new PropertyDetail(
                prop.path("id").asLong(),
                prop.path("bedrooms").asInt(),
                prop.path("bathrooms").asInt(),
                address.path("displayAddress").asText(""),
                address.path("outcode").asText(""),
                address.path("ukCountry").asText(""),
                prices.path("primaryPrice").asText(""),
                prices.path("displayPriceQualifier").asText(""),
                location.path("latitude").asDouble(),
                location.path("longitude").asDouble(),
                text.path("description").asText(""),
                text.path("propertyPhrase").asText(""),
                text.path("pageTitle").asText(""),
                prop.path("propertySubType").asText(""),
                tenure.path("tenureType").asText(""),
                tenure.path("yearsRemainingOnLease").asInt(),
                customer.path("branchDisplayName").asText(""),
                contactInfo.path("telephoneNumbers").path("localNumber").asText(""),
                keyFeatures,
                imageUrls,
                floorplanUrls,
                stations,
                listingHistory.path("listingUpdateReason").asText(""),
                status.path("published").asBoolean(false)
        );
    }

    private JsonNode[] parsePageModelNodes(String html) throws Exception {
        String marker = "window.__PAGE_MODEL = ";
        int start = html.indexOf(marker);
        if (start == -1) {
            throw new IllegalStateException("__PAGE_MODEL not found in Rightmove response");
        }
        start += marker.length();
        int scriptClose = html.indexOf("</script>", start);
        if (scriptClose == -1) {
            throw new IllegalStateException("__PAGE_MODEL script close tag not found");
        }
        String raw = html.substring(start, scriptClose).stripTrailing();
        if (raw.endsWith(";")) {
            raw = raw.substring(0, raw.length() - 1);
        }
        JsonNode pageModel = objectMapper.readTree(raw);
        JsonNode dataArray = objectMapper.readTree(pageModel.get("data").asText());
        JsonNode[] nodes = new JsonNode[dataArray.size()];
        for (int i = 0; i < dataArray.size(); i++) {
            nodes[i] = dataArray.get(i);
        }
        return nodes;
    }

    /**
     * Decodes a node from the __PAGE_MODEL flat reference array.
     * Integer values inside objects/arrays are indices into the nodes array (references).
     * Integer values that are the node itself are literal values (e.g. bedrooms = 3).
     */
    private JsonNode resolveNode(JsonNode[] nodes, JsonNode node) {
        if (node == null || node.isNull() || node.isTextual() || node.isBoolean()
                || node.isIntegralNumber() || node.isFloatingPointNumber()) {
            return node;
        }
        if (node.isArray()) {
            ArrayNode result = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                if (item.isIntegralNumber()) {
                    long idx = item.asLong();
                    result.add(idx >= 0 && idx < nodes.length
                            ? resolveNode(nodes, nodes[(int) idx])
                            : item);
                } else {
                    result.add(resolveNode(nodes, item));
                }
            }
            return result;
        }
        if (node.isObject()) {
            ObjectNode result = objectMapper.createObjectNode();
            node.properties().forEach(entry -> {
                JsonNode value = entry.getValue();
                if (value.isIntegralNumber()) {
                    long idx = value.asLong();
                    result.set(entry.getKey(), idx >= 0 && idx < nodes.length
                            ? resolveNode(nodes, nodes[(int) idx])
                            : value);
                } else {
                    result.set(entry.getKey(), resolveNode(nodes, value));
                }
            });
            return result;
        }
        return node;
    }
}
