package org.samaan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
@Data
@Document(collection = "payments")
public class PaymentDetails {
    @Id
    private String id;
    private String tripId;
    private String orderId;
    private String paymentId;
    private String status;
    private double paidAmount;
    private String paymentDate;
    private String senderEmail;
    // Getters and Setters
    // ...
}

