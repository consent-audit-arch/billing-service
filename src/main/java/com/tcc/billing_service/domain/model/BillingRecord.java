package com.tcc.billing_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class BillingRecord {
    private Long id;
    private String dataSubjectId;
    private String description;
    private BigDecimal amount;
    private String status;
    private Instant createdAt;

    public BillingRecord() {
    }

    public BillingRecord(String dataSubjectId, String description, BigDecimal amount) {
        this.dataSubjectId = dataSubjectId;
        this.description = description;
        this.amount = amount;
        this.status = "PENDING";
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDataSubjectId() {
        return dataSubjectId;
    }

    public void setDataSubjectId(String dataSubjectId) {
        this.dataSubjectId = dataSubjectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
