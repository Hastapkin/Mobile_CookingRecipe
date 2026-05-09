package com.example.cookingrecipe.data.network;

import android.content.Context;

import com.example.cookingrecipe.R;
import com.example.cookingrecipe.data.storage.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    private static ApiService apiService;

    public static void init(Context context) {
        if (retrofit != null) return;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            String token = SessionManager.getInstance() != null ? SessionManager.getInstance().getToken() : null;
            if (token != null && !token.isEmpty()) {
                Request updated = original.newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(updated);
            }
            return chain.proceed(original);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();

        String baseUrl = context.getString(R.string.api_base_url);
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static ApiService getApiService() {
        return apiService;
    }
}
