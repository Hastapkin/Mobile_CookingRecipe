package com.example.cookingrecipe.data.model;

public class CourseLearningLesson {
    public int id;
    public String title;
    public String description;
    public int order;
    public String contentType;
    public Integer durationMinutes;
    public boolean isCompleted;
    public Integer score;
    public LessonContent content;

    public static class LessonContent {
        public String articleText;
        public String videoUrl;
        public Integer videoDuration;
        public AssignmentQuestion[] assignmentQuestions;
        public int passingScore;
    }
}
