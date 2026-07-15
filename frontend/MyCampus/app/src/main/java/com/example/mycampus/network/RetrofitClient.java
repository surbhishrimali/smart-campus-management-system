package com.example.mycampus.network;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Live PythonAnywhere API url
    private static final String BASE_URL = "https://ietcampus.pythonanywhere.com/";

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public static ApiService getApiServiceWithToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MY_CAMPUS_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", "");

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", token)
                            .build();
                    return chain.proceed(newRequest);
                }).build();

        Retrofit authRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        return authRetrofit.create(ApiService.class);
    }
}
