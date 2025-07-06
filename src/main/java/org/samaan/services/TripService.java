package org.samaan.services;

import org.samaan.dto.RatingRequest;
import org.samaan.model.Trip;
import org.samaan.model.User;
import org.samaan.repositories.TripRepository;
import org.samaan.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PriceCalculator priceCalculator;

    public Trip addTripWithPrice(Trip trip) {
        User user = userRepository.findByEmail(trip.getEmail());
        if (user == null) throw new RuntimeException("User not found");

        trip.setCarrierName(user.getName());
        trip.setPrice(priceCalculator.calculatePrice(trip.getSource(), trip.getDestination()));
        return tripRepository.save(trip);
    }

    public ResponseEntity<?> cancelTripByRole(String tripId, String role) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isEmpty()) return ResponseEntity.notFound().build();

        Trip trip = optionalTrip.get();

        if ("sender".equalsIgnoreCase(role)) {
            trip.setSenderRequestedCancel(true);
        } else if ("carrier".equalsIgnoreCase(role)) {
            trip.setCarrierRequestedCancel(true);
        } else {
            return ResponseEntity.badRequest().body("Invalid role");
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

    public ResponseEntity<String> acceptSender(String tripId) {
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.notFound().build();

        Trip trip = tripOpt.get();
        trip.setCarrierAccepted(true);
        tripRepository.save(trip);
        return ResponseEntity.ok("Sender accepted successfully");
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public List<Trip> getTripsByEmail(String email) {
        return tripRepository.findByEmail(email);
    }

    public ResponseEntity<List<Trip>> getTripsByEmailResponse(String email) {
        List<Trip> trips = tripRepository.findByEmail(email);
        if (trips.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(trips);
    }

    public List<Trip> searchAvailableTrips(String source, String destination, String date) {
        List<Trip> trips = tripRepository.findBySourceAndDestinationAndDate(source, destination, date);
        return trips.stream().filter(t -> !t.isSenderSelected()).collect(Collectors.toList());
    }

    public ResponseEntity<String> completeTrip(String tripId) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();
            trip.setCarrierCompleted(true);
            tripRepository.save(trip);
            return ResponseEntity.ok("Trip marked as completed");
        }
        return ResponseEntity.status(404).body("Trip not found");
    }

    public ResponseEntity<List<Trip>> getTripsBySenderEmailResponse(String senderEmail) {
        List<Trip> trips = tripRepository.findBySenderEmail(senderEmail);
        if (trips.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(trips);
    }

    public ResponseEntity<?> rateTrip(String tripId, RatingRequest request) {
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.status(404).body("Trip not found");

        Trip trip = tripOpt.get();
        if (trip.getRating() != null) {
            return ResponseEntity.badRequest().body("Trip is already rated.");
        }

        trip.setRating(request.getRating());
        trip.setFeedback(request.getFeedback());
        tripRepository.save(trip);

        return ResponseEntity.ok("Rating and feedback submitted successfully.");
    }

    public List<Trip> getSenderRatedTrips(String senderEmail) {
        return tripRepository.findBySenderEmailAndRatingNotNull(senderEmail);
    }

    public ResponseEntity<List<Trip>> getCompletedTripsByCarrier(Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) return ResponseEntity.badRequest().build();

        List<Trip> trips = tripRepository.findByEmailAndCarrierCompletedTrue(email);
        return ResponseEntity.ok(trips);
    }

    public ResponseEntity<String> selectTrip(String tripId, String senderEmail) {
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.status(404).body("Trip not found");

        Trip trip = tripOpt.get();
        trip.setSenderEmail(senderEmail);
        trip.setSenderSelected(true);
        tripRepository.save(trip);

        return ResponseEntity.ok("Trip selected successfully");
    }
}
