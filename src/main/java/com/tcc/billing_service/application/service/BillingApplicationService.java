package com.tcc.billing_service.application.service;

import com.tcc.billing_service.application.dto.*;
import com.tcc.billing_service.application.dto.batch.BatchBillingRequest;
import com.tcc.billing_service.application.dto.batch.BatchBillingResponse;
import com.tcc.billing_service.application.dto.batch.DeniedUser;
import com.tcc.billing_service.domain.exception.BillingRecordNotFoundException;
import com.tcc.billing_service.domain.model.BillingRecord;
import com.tcc.billing_service.domain.repository.BillingRecordRepository;
import com.tcc.billing_service.infrastructure.client.UserServiceClient;
import com.tcc.security.aspect.ConsentAuthorizationAspect;
import com.tcc.security.pip.PipTitularResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    public BillingWithProfileResponse findWithProfile(Long id, String purpose, List<String> dataCategories, String correlationId) {
        BillingRecordResponse billing = findById(id);

        String resolvedCorrelationId = correlationId != null ? correlationId : java.util.UUID.randomUUID().toString();
        UserProfileResponse profile = userServiceClient.fetchUserProfile(
                billing.getDataSubjectId(), purpose, dataCategories, resolvedCorrelationId);

        return new BillingWithProfileResponse(billing, profile);
    }

    @Transactional(readOnly = true)
    public BatchBillingResponse findBatch(BatchBillingRequest request, String purpose,
                                           List<String> dataCategories, String correlationId) {
        List<PipTitularResult> decisions = ConsentAuthorizationAspect.getDecisionsFromRequest();
        String resolvedCorrelationId = correlationId != null ? correlationId : java.util.UUID.randomUUID().toString();

        List<Long> requestedIds = request.getIds();
        List<Long> authorizedIds;
        List<DeniedUser> denied;

        if (!decisions.isEmpty()) {
            authorizedIds = decisions.stream()
                    .filter(PipTitularResult::isAuthorized)
                    .map(PipTitularResult::getTitularId)
                    .toList();
            denied = decisions.stream()
                    .filter(d -> !d.isAuthorized())
                    .map(d -> new DeniedUser(d.getTitularId(), d.getReason()))
                    .toList();
        } else {
            authorizedIds = requestedIds;
            denied = List.of();
        }

        UserServiceBatchResponse userBatch = userServiceClient.fetchBatchUserProfiles(
                authorizedIds, purpose, dataCategories, resolvedCorrelationId);

        List<DeniedUser> allDenied = new ArrayList<>(denied);
        if (userBatch != null && userBatch.getDenied() != null) {
            allDenied.addAll(userBatch.getDenied());
        }
        return new BatchBillingResponse(userBatch.getData(), allDenied);
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
