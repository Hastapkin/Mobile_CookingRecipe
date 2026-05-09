package com.example.cookingrecipe.data.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.cookingrecipe.data.model.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREFS_NAME = "cooking_recipe_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";

    private static SessionManager instance;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public void saveSession(User user, String token) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, gson.toJson(user))
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    @Nullable
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    @Nullable
    public User getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAuthenticated() {
        return getToken() != null && getUser() != null;
    }
}
