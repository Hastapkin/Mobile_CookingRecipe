package com.example.cookingrecipe;

import android.app.Application;

import com.example.cookingrecipe.data.network.ApiClient;
import com.example.cookingrecipe.data.storage.SessionManager;

public class CookingRecipeApplication extends Application {
    private static CookingRecipeApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SessionManager.init(this);
        ApiClient.init(this);
    }

    public static CookingRecipeApplication getInstance() {
        return instance;
    }
}
