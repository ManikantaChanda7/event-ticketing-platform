package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
public class CityController {

        private final List<Map<String, Object>> cities = List.of(
                        Map.of("City", "Hyderabad", "State", "Telangana", "Long", 78.456355, "Lat", 17.384052),
                        Map.of("City", "Bengaluru", "State", "Karnataka", "Long", 77.587106, "Lat", 12.977063),
                        Map.of("City", "Chennai", "State", "Tamil Nadu", "Long", 80.248357, "Lat", 13.084622),
                        Map.of("City", "Mumbai", "State", "Maharashtra", "Long", 72.836447, "Lat", 18.987807),
                        Map.of("City", "Delhi", "State", "Delhi", "Long", 77.2, "Lat", 28.6),
                        Map.of("City", "Pune", "State", "Maharashtra", "Long", 73.849852, "Lat", 18.513271),
                        Map.of("City", "Kolkata", "State", "West Bengal", "Long", 88.363044, "Lat", 22.562627),
                        Map.of("City", "Ahmedabad", "State", "Gujarat", "Long", 72.587265, "Lat", 23.025793),
                        Map.of("City", "Chandigarh", "State", "Chandigarh", "Long", 76.788398, "Lat", 30.736292),
                        Map.of("City", "New Delhi", "State", "Delhi", "Long", 77.2, "Lat", 28.6));

        @GetMapping("/search")
        public ApiResponse<List<Map<String, Object>>> searchCities(
                        @RequestParam String q) {
                List<Map<String, Object>> response = cities.stream()
                                .filter(city -> city.get("City").toString().toLowerCase().contains(q.toLowerCase()))
                                .limit(10)
                                .toList();
                return new ApiResponse<>(true, "Cities searched successfully", response);
        }
}
