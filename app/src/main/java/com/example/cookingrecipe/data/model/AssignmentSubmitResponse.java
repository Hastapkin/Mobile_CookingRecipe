package com.example.cookingrecipe.data.model;

public class AssignmentSubmitResponse {
    public boolean success;
    public Data data;

    public static class Data {
        public int score;
        public boolean passed;
        public int passingScore;
        public CourseLearningDetail learning;
    }
}
