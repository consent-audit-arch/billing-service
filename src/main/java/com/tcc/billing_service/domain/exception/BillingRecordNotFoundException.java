package com.tcc.billing_service.domain.exception;

public class BillingRecordNotFoundException extends RuntimeException {
    public BillingRecordNotFoundException(Long id) {
        super("Billing record not found with id: " + id);
    }
}
