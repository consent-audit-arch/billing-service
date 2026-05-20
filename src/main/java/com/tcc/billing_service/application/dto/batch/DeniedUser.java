package com.tcc.billing_service.application.dto.batch;

public class DeniedUser {
    private Long id;
    private String reason;

    public DeniedUser() {}

    public DeniedUser(Long id, String reason) {
        this.id = id;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
