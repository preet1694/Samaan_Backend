package org.samaan.repositories;

import org.samaan.model.PaymentDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<PaymentDetails, String> {
    PaymentDetails findByPaymentId(String paymentId);
}

