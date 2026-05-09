package com.example.cookingrecipe.data.model;

import java.util.List;

public class CoursesOverviewResponse {
    public boolean success;
    public Data data;

    public static class Data {
        public List<CourseOverview> courses;
        public Pagination pagination;
    }

    public static class Pagination {
        public int page;
        public int limit;
        public int total;
        public int totalPages;
    }
}
