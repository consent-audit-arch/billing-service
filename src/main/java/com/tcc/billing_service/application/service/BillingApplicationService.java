package com.tcc.billing_service.application.service;

import com.tcc.billing_service.application.dto.*;
import com.tcc.billing_service.domain.exception.BillingRecordNotFoundException;
import com.tcc.billing_service.domain.model.BillingRecord;
import com.tcc.billing_service.domain.repository.BillingRecordRepository;
import com.tcc.billing_service.infrastructure.client.UserServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BillingApplicationService {

    private final BillingRecordRepository billingRecordRepository;
    private final UserServiceClient userServiceClient;

    public BillingApplicationService(BillingRecordRepository billingRecordRepository,
                                     UserServiceClient userServiceClient) {
        this.billingRecordRepository = billingRecordRepository;
        this.userServiceClient = userServiceClient;
    }

    public BillingRecordResponse create(CreateBillingRecordCommand command) {
        BillingRecord record = new BillingRecord(
                command.getDataSubjectId(),
                command.getDescription(),
                command.getAmount()
        );
        BillingRecord saved = billingRecordRepository.save(record);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BillingRecordResponse findById(Long id) {
        BillingRecord record = billingRecordRepository.findById(id)
                .orElseThrow(() -> new BillingRecordNotFoundException(id));
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public BillingWithProfileResponse findWithProfile(Long id, String purpose, String correlationId) {
        BillingRecordResponse billing = findById(id);

        String resolvedCorrelationId = correlationId != null ? correlationId : java.util.UUID.randomUUID().toString();
        UserProfileResponse profile = userServiceClient.fetchUserProfile(
                billing.getDataSubjectId(), purpose, resolvedCorrelationId);

        return new BillingWithProfileResponse(billing, profile);
    }

    private BillingRecordResponse toResponse(BillingRecord record) {
        BillingRecordResponse response = new BillingRecordResponse();
        response.setId(record.getId());
        response.setDataSubjectId(record.getDataSubjectId());
        response.setDescription(record.getDescription());
        response.setAmount(record.getAmount());
        response.setStatus(record.getStatus());
        response.setCreatedAt(record.getCreatedAt());
        return response;
    }
}
