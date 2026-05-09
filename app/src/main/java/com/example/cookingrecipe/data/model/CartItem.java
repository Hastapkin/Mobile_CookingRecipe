package com.example.cookingrecipe.data.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    public int id;

    @SerializedName(value = "cartId", alternate = {"cartid", "itemId"})
    public int cartId;

    @SerializedName(value = "courseId", alternate = {"courseid"})
    public int courseId;

    @SerializedName(value = "recipeId", alternate = {"recipeid"})
    public int recipeId;

    public String title;
    public String thumbnail;
    public String videoThumbnail;

    @SerializedName(value = "price", alternate = {"priceValue", "originalPrice"})
    public double price;

    @SerializedName(value = "discountedPrice", alternate = {"discountedprice"})
    public Double discountedPrice;

    @SerializedName(value = "difficulty", alternate = {"difficultyLevel"})
    public String difficulty;

    @SerializedName(value = "lessonCount", alternate = {"lesson_count"})
    public Integer lessonCount;

    @SerializedName(value = "estimatedDurationMinutes", alternate = {"durationminutes", "durationMinutes"})
    public Integer estimatedDurationMinutes;

    public Integer cookingTime;
    public String category;
}
