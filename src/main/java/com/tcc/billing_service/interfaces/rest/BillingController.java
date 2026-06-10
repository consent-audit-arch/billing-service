package com.tcc.billing_service.interfaces.rest;

import com.tcc.billing_service.application.dto.BillingRecordResponse;
import com.tcc.billing_service.application.dto.BillingWithProfileResponse;
import com.tcc.billing_service.application.dto.CreateBillingRecordCommand;
import com.tcc.billing_service.application.dto.batch.BatchBillingRequest;
import com.tcc.billing_service.application.dto.batch.BatchBillingResponse;
import com.tcc.billing_service.application.service.BillingApplicationService;
import com.tcc.security.annotation.RequiresConsent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final BillingApplicationService billingService;

    public BillingController(BillingApplicationService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    public ResponseEntity<BillingRecordResponse> create(@Valid @RequestBody CreateBillingRecordCommand command) {
        BillingRecordResponse response = billingService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillingRecordResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.findById(id));
    }

    @GetMapping("/{id}/with-profile")
    public ResponseEntity<BillingWithProfileResponse> findWithProfile(
            @PathVariable Long id,
            HttpServletRequest request) {
        String purpose = request.getHeader("X-Purpose");
        String correlationId = request.getHeader("X-Correlation-Id");
        List<String> dataCategories = parseDataCategoriesHeader(request.getHeader("X-Data-Category"));
        return ResponseEntity.ok(billingService.findWithProfile(id, purpose, dataCategories, correlationId));
    }

    @GetMapping("/users/{userId}/billing/{billingId}/with-profile")
    @RequiresConsent(resource = "BILLING_RECORD", action = "READ",
            dataCategories = {"FINANCIAL_DATA", "PERSONAL_DATA"},
            dataSubjectIdParam = "userId")
    public ResponseEntity<BillingWithProfileResponse> findWithProfileByUser(
            @PathVariable Long userId,
            @PathVariable Long billingId,
            HttpServletRequest request) {
        String purpose = request.getHeader("X-Purpose");
        String correlationId = request.getHeader("X-Correlation-Id");
        List<String> dataCategories = parseDataCategoriesHeader(request.getHeader("X-Data-Category"));
        return ResponseEntity.ok(billingService.findWithProfileByUser(userId, billingId, purpose, dataCategories, correlationId));
    }

    @PostMapping("/batch")
    @RequiresConsent(resource = "BILLING_RECORD", action = "READ",
            dataCategories = {"FINANCIAL_DATA", "PERSONAL_DATA"},
            dataSubjectIdsParam = "request.ids")
    public ResponseEntity<BatchBillingResponse> findBatch(
            @Valid @RequestBody BatchBillingRequest request,
            HttpServletRequest httpRequest) {
        String purpose = httpRequest.getHeader("X-Purpose");
        String correlationId = httpRequest.getHeader("X-Correlation-Id");
        List<String> dataCategories = parseDataCategoriesHeader(httpRequest.getHeader("X-Data-Categories"));
        return ResponseEntity.ok(billingService.findBatch(request, purpose, dataCategories, correlationId));
    }

    private List<String> parseDataCategoriesHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(headerValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
