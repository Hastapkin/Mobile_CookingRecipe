package com.example.cookingrecipe.data.model;

import java.util.List;

public class CourseReviewsResponse {
    public boolean success;
    public Data data;

    public static class Data {
        public Summary summary;
        public boolean canReview;
        public CourseReview myReview;
        public List<CourseReview> reviews;
    }

    public static class Summary {
        public double rating;
        public int count;
    }
}
