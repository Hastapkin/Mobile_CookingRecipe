package com.example.cookingrecipe.data.model;

public class RegisterRequest {
    public String name;
    public String email;
    public String username;
    public String password;

    public RegisterRequest(String name, String email, String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
