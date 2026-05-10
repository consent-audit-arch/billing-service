package com.tcc.billing_service.interfaces.rest;

import com.tcc.billing_service.application.dto.BillingRecordResponse;
import com.tcc.billing_service.application.dto.BillingWithProfileResponse;
import com.tcc.billing_service.application.dto.CreateBillingRecordCommand;
import com.tcc.billing_service.application.service.BillingApplicationService;
import com.tcc.security.annotation.RequiresConsent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @RequiresConsent(resource = "BILLING_RECORD", action = "READ")
    public ResponseEntity<BillingWithProfileResponse> findWithProfile(
            @PathVariable Long id,
            HttpServletRequest request) {
        String purpose = request.getHeader("X-Purpose");
        String correlationId = request.getHeader("X-Correlation-Id");
        return ResponseEntity.ok(billingService.findWithProfile(id, purpose, correlationId));
    }
}
