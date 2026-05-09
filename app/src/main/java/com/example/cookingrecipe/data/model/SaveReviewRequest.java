package com.example.cookingrecipe.data.model;

public class SaveReviewRequest {
    public int rating;
    public String comment;

    public SaveReviewRequest(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
