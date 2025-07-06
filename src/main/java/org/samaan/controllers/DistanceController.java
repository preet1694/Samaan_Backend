package org.samaan.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.samaan.services.DistanceService;

@RestController
@RequestMapping("/distance")
public class DistanceController {

    @Autowired
    private DistanceService distanceService;

    @GetMapping
    public ResponseEntity<String> getDistance(@RequestParam String location1, @RequestParam String location2) {
        return distanceService.calculateDistanceAndCost(location1, location2);
    }
}
