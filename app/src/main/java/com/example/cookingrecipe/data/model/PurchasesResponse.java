package com.example.cookingrecipe.data.model;

import java.util.List;

public class PurchasesResponse {
    public boolean success;
    public Data data;

    public static class Data {
        public List<Integer> courseIds;
    }
}
