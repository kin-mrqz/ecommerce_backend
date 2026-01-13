package com.kin.ecommerce.backend.api.model;

public class LoginResponse {
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    private String jwt;
}
