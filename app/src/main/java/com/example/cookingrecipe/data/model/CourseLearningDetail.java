package com.example.cookingrecipe.data.model;

import java.util.List;

public class CourseLearningDetail {
    public CourseLearningCourse course;
    public List<CourseLearningModule> modules;
    public Progress progress;

    public static class CourseLearningCourse {
        public int id;
        public String title;
        public String description;
        public String thumbnail;
        public String difficulty;
        public Integer duration;
        public Integer moduleCount;
    }

    public static class Progress {
        public int completedLessons;
        public int totalLessons;
        public double percent;
    }
}
