package com.kin.ecommerce.backend.api.model;

public class LoginResponse {
    private String jwt;
    private boolean success;
    private String failureResponse;

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFailureResponse() {
        return failureResponse;
    }

    public void setFailureResponse(String failureResponse) {
        this.failureResponse = failureResponse;
    }
}
