package org.samaan.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.samaan.model.LatLng;

@Service
public class DistanceService {

    @Autowired
    private GeocodingService geocodingService;

    public ResponseEntity<String> calculateDistanceAndCost(String location1, String location2) {
        LatLng coordinates1 = geocodingService.getCoordinates(location1);
        LatLng coordinates2 = geocodingService.getCoordinates(location2);

        if (coordinates1 == null || coordinates2 == null) {
            return ResponseEntity.badRequest().body("Unable to fetch coordinates for one or both locations.");
        }

        double distance = DistanceCalculator.calculateDistance(
                coordinates1.getLat(), coordinates1.getLng(),
                coordinates2.getLat(), coordinates2.getLng());

        int cost = (int) (distance * 0.38);

        return ResponseEntity.ok("Distance: " + distance + " km\nCost: ₹" + cost);
    }
}
