package org.samaan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
@Data
@Document("payouts")
public class PayoutDetails {
    @Id
    private String payoutId; // Razorpay payout ID
    private String contactId;
    private String fundAccountId;
    private String name;
    private String email;
    private String upi;
    private int amount; // in paise
    private String status;
}

