package com.tcc.billing_service.application.dto.batch;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BatchBillingRequest {
    @NotEmpty(message = "ids must not be empty")
    private List<Long> ids;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}
