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
public class LocationService {

    @Inject
    RightmoveClient rightmoveClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Location> searchLocations(String query, int limit) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://los.rightmove.co.uk/typeahead?query=" + encodedQuery + "&limit=" + limit + "&exclude=STREET";
        String body = rightmoveClient.get(url);

p        JsonNode root = objectMapper.readTree(body);
        JsonNode matches = root.get("matches");

        List<Location> locations = new ArrayList<>();
        for (JsonNode node : matches) {
            locations.add(new Location(
                    node.get("displayName").asText(),
                    node.get("id").asText(),
                    node.has("type") ? node.get("type").asText() : "N/A"
            ));
        }
        return locations;
    }
}
