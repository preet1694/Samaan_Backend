package org.samaan.controllers;

import org.samaan.dto.RatingRequest;
import org.samaan.dto.SelectionRequest;
import org.samaan.model.Trip;
import org.samaan.repositories.TripRepository;
import org.samaan.services.PriceCalculator;
import org.samaan.services.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PriceCalculator priceCalculator;

    @PostMapping("/add")
    public ResponseEntity<Trip> addTrip(@Valid @RequestBody Trip trip) {
        double price = priceCalculator.calculatePrice(trip.getSource(), trip.getDestination());
        trip.setPrice(price);

        Trip savedTrip = tripService.addTrip(trip);
        return ResponseEntity.ok(savedTrip);
    }

    @PostMapping("/{tripId}/cancel")
    public ResponseEntity<?> requestCancel(@PathVariable String tripId, @RequestParam String role) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Trip trip = optionalTrip.get();

        if ("sender".equalsIgnoreCase(role)) {
            trip.setSenderRequestedCancel(true);
        } else if ("carrier".equalsIgnoreCase(role)) {
            trip.setCarrierRequestedCancel(true);
        } else {
            return ResponseEntity.badRequest().body("Invalid role specified. Must be 'sender' or 'carrier'.");
        }
        if (trip.isSenderRequestedCancel() && trip.isCarrierRequestedCancel() && trip.isCarrierAccepted()) {
            trip.setSenderSelected(false);
            trip.setCarrierAccepted(false);
            trip.setSenderRequestedCancel(false);
            trip.setCarrierRequestedCancel(false);
            trip.setSenderEmail(null);
        }
        tripRepository.save(trip);
        return ResponseEntity.ok(trip);
    }

    @PutMapping("/accept-sender/{tripId}")
    public ResponseEntity<String> acceptSender(@PathVariable String tripId) {
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.notFound().build();

        Trip trip = tripOpt.get();
        trip.setCarrierAccepted(true);
        tripRepository.save(trip);
        return ResponseEntity.ok("Sender accepted successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }
    
    @GetMapping("/getusertrips")
    public ResponseEntity<List<Trip>> getTripsByEmail(@RequestParam String storedEmail) {
        List<Trip> trips = tripService.getTripsByEmail(storedEmail);
        if (trips.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchTrips(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam String date
    ) {
        List<Trip> trips = tripRepository.findBySourceAndDestinationAndDate(source, destination, date);

        // Only include trips that have not been selected by any sender
        List<Trip> availableTrips = trips.stream()
                .filter(trip -> !trip.isSenderSelected()) // Only trips where senderSelected is false
                .collect(Collectors.toList());

        return ResponseEntity.ok(availableTrips);
    }

    @PutMapping("/complete/{tripId}")
    public ResponseEntity<String> completeTrip(@PathVariable String tripId) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();
            trip.setCarrierCompleted(true); 
            tripRepository.save(trip);
            return ResponseEntity.ok("Trip marked as completed");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trip not found");
    }

    @GetMapping("/sender/{senderEmail}")
    public ResponseEntity<List<Trip>> getTripsBySenderEmail(@PathVariable String senderEmail) {
        List<Trip> trips = tripRepository.findBySenderEmail(senderEmail);
        if (trips.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(trips);
    }

    @PostMapping("rating/{tripId}")
    public ResponseEntity<?> rateTrip(@PathVariable String tripId, @RequestBody RatingRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getRating() != null) {
            return ResponseEntity.badRequest().body("Trip is already rated.");
        }

        trip.setRating(request.getRating());
        trip.setFeedback(request.getFeedback());
        tripRepository.save(trip);

        return ResponseEntity.ok("Rating and feedback submitted successfully.");
    }

    @GetMapping("ratings/{senderEmail}")
    public List<Trip> getSenderRatings(@PathVariable String senderEmail) {
        return tripRepository.findBySenderEmailAndRatingNotNull(senderEmail);
    }

    @PostMapping("/select")
    public ResponseEntity<String> selectTrip(@RequestBody SelectionRequest request) {
        Optional<Trip> optionalTrip = tripRepository.findById(request.getTripId());
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();
            trip.setSenderEmail(request.getSenderEmail());
            trip.setSenderSelected(true);
            tripRepository.save(trip);
            return ResponseEntity.ok("Trip selected successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trip not found");
    }

}
