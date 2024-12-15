package com.example.accountbook_java_edit_ver;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl) // Swagger에서 제공된 API 기본 경로
                    .addConverterFactory(ScalarsConverterFactory.create()) // 문자열 응답 지원
                    .addConverterFactory(GsonConverterFactory.create())    // JSON 응답 지원
                    .build();
        }
        return retrofit;
    }
}
