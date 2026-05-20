package com.tcc.billing_service.application.dto;

import com.tcc.billing_service.application.dto.batch.DeniedUser;
import java.util.List;

public class UserServiceBatchResponse {
    private List<UserProfileResponse> data;
    private List<DeniedUser> denied;
    private boolean partial;

    public List<UserProfileResponse> getData() { return data; }
    public void setData(List<UserProfileResponse> data) { this.data = data; }
    public List<DeniedUser> getDenied() { return denied; }
    public void setDenied(List<DeniedUser> denied) { this.denied = denied; }
    public boolean isPartial() { return partial; }
    public void setPartial(boolean partial) { this.partial = partial; }
}
