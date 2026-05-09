package com.example.cookingrecipe.data.model;

public class UploadProfilePictureResponse {
    public boolean success;
    public String message;
    public Data data;

    public static class Data {
        public String imageUrl;
        public String publicId;
    }
}
