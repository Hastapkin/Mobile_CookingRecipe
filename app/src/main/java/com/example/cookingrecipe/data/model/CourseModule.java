package com.example.cookingrecipe.data.model;

import java.util.List;

public class CourseModule {
    public int id;
    public String title;
    public String description;
    public int order;
    public String updatedAt;
    public List<CourseLesson> lessons;
}
