package com.example.cookingrecipe.data.model;

import java.util.List;

public class CourseOverviewDetail {
    public CourseDetail course;
    public List<CourseModule> modules;

    public static class CourseDetail {
        public int id;
        public String title;
        public String description;
        public String thumbnail;
        public Double price;
        public String difficulty;
        public Integer duration;
        public Integer moduleCount;
        public String category;
        public Integer viewCount;
        public Integer purchaseCount;
        public Double rating;
        public String createdAt;
        public String updatedAt;
    }
}
