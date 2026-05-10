package com.tcc.billing_service.domain.repository;

import com.tcc.billing_service.domain.model.BillingRecord;

import java.util.Optional;

public interface BillingRecordRepository {
    BillingRecord save(BillingRecord record);
    Optional<BillingRecord> findById(Long id);
}
