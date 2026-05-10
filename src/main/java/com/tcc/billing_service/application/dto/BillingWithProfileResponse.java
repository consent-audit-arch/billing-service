package com.tcc.billing_service.application.dto;

public class BillingWithProfileResponse {
    private BillingRecordResponse billingRecord;
    private UserProfileResponse userProfile;

    public BillingWithProfileResponse(BillingRecordResponse billingRecord, UserProfileResponse userProfile) {
        this.billingRecord = billingRecord;
        this.userProfile = userProfile;
    }

    public BillingRecordResponse getBillingRecord() {
        return billingRecord;
    }

    public UserProfileResponse getUserProfile() {
        return userProfile;
    }
}
