package com.example.cookingrecipe.data.model;

import java.util.List;

public class Transaction {
    public int id;
    public int userId;
    public double totalAmount;
    public String paymentMethod;
    public String paymentProof;
    public String status;
    public String createdAt;
    public String verifiedAt;
    public Integer verifiedBy;
    public Integer recipeCount;
    public Integer courseCount;
    public List<PurchaseItem> courses;
    public List<PurchaseItem> recipes;

    public static class PurchaseItem {
        public Integer courseId;
        public Integer recipeId;
        public String title;
        public String videoThumbnail;
        public String thumbnail;
        public double price;
    }
}
