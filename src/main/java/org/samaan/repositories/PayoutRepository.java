package org.samaan.repositories;

import org.samaan.model.PayoutDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PayoutRepository extends MongoRepository<PayoutDetails, String> {
    PayoutDetails findByPayoutId(String payoutId);
}
