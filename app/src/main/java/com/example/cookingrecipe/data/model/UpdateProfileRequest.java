package com.example.cookingrecipe.data.model;

public class UpdateProfileRequest {
    public String name;
    public String email;

    public UpdateProfileRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
