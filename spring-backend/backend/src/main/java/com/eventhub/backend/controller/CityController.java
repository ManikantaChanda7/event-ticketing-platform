package com.eventhub.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    @GetMapping("/search")
    public ResponseEntity<List<String>> searchCities(
            @RequestParam String q) {

        List<String> cities = List.of(
                "Hyderabad",
                "Bangalore",
                "Chennai",
                "Mumbai",
                "Delhi",
                "Pune",
                "Kolkata");

        return ResponseEntity.ok(
                cities.stream()
                        .filter(city -> city.toLowerCase()
                                .contains(
                                        q.toLowerCase()))
                        .limit(10)
                        .toList());
    }
}
