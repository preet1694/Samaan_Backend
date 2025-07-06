package org.samaan.controllers;

import org.samaan.dto.RatingRequest;
import org.samaan.dto.SelectionRequest;
import org.samaan.model.Trip;
import org.samaan.services.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @PostMapping("/add")
    public ResponseEntity<Trip> addTrip(@Valid @RequestBody Trip trip) {
        return ResponseEntity.ok(tripService.addTripWithPrice(trip));
    }

    @PostMapping("/{tripId}/cancel")
    public ResponseEntity<?> requestCancel(@PathVariable String tripId, @RequestParam String role) {
        return tripService.cancelTripByRole(tripId, role);
    }

    @PutMapping("/accept-sender/{tripId}")
    public ResponseEntity<String> acceptSender(@PathVariable String tripId) {
        return tripService.acceptSender(tripId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }

    @GetMapping("/getusertrips")
    public ResponseEntity<List<Trip>> getTripsByEmail(@RequestParam String storedEmail) {
        return tripService.getTripsByEmailResponse(storedEmail);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchTrips(@RequestParam String source,
                                                  @RequestParam String destination,
                                                  @RequestParam String date) {
        return ResponseEntity.ok(tripService.searchAvailableTrips(source, destination, date));
    }

    @PutMapping("/complete/{tripId}")
    public ResponseEntity<String> completeTrip(@PathVariable String tripId) {
        return tripService.completeTrip(tripId);
    }

    @GetMapping("/sender/{senderEmail}")
    public ResponseEntity<List<Trip>> getTripsBySenderEmail(@PathVariable String senderEmail) {
        return tripService.getTripsBySenderEmailResponse(senderEmail);
    }

    @PostMapping("rating/{tripId}")
    public ResponseEntity<?> rateTrip(@PathVariable String tripId, @RequestBody RatingRequest request) {
        return tripService.rateTrip(tripId, request);
    }

    @GetMapping("ratings/{senderEmail}")
    public List<Trip> getSenderRatings(@PathVariable String senderEmail) {
        return tripService.getSenderRatedTrips(senderEmail);
    }

    @PostMapping("/completed/byCarrier")
    public ResponseEntity<List<Trip>> getCompletedTripsByCarrier(@RequestBody Map<String, String> request) {
        return tripService.getCompletedTripsByCarrier(request);
    }

    @PostMapping("/select")
    public ResponseEntity<String> selectTrip(@RequestBody SelectionRequest request) {
        return tripService.selectTrip(request.getTripId(), request.getSenderEmail());
    }
}
