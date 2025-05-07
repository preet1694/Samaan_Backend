package org.samaan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Document(collection = "trips")
public class Trip {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Carrier username is required")
    private String carrierUsername;

    @NotBlank(message = "Carrier name is required")
    private String carrierName;

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "startLandmark is required")
    private String startLandmark;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "endLandmark is required")
    private String endLandmark;

    @NotBlank(message = "email is required")
    private String email;

    @NotNull(message = "Date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // Ensure date is stored as 'YYYY-MM-DD'
    private String date;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotBlank(message = "Capacity is required")
    private String capacity;
    private String feedback;
    private Integer rating;
    private double price;
    private boolean senderSelected;
    private boolean carrierCompleted;
    private String senderEmail;
    private boolean carrierAccepted = false;

    private boolean senderRequestedCancel = false;
    private boolean carrierRequestedCancel = false;
    private boolean paid = false;
    private double paidAmount;
    private String razorpayPaymentId;
    private String paymentDate;

}
