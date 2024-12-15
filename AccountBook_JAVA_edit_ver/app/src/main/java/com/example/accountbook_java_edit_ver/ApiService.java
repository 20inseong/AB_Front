package com.example.accountbook_java_edit_ver;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

public interface ApiService {
    // 로그인 API 정의
    @POST("members/login")
    Call<String> login(@Body LoginRequest loginRequest);

    // 회원가입 API 정의
    @POST("members/register")
    Call<Void> signUp(@Body MemberRequest memberRequest);

    // 캘린더 수입 날짜
    @GET("api/incomes/user/{userId}/range")
    Call<List<Records>> getMonthlyIncomeRecords(
            @Path("userId") String userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/incomes/user/{userId}/range")
    Call<List<Records>> getWeeklyIncomeRecords(
            @Path("userId") String userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/incomes/user/{userId}")
    Call<List<DetailRecords>> getDailyIncomeRecords(
            @Path("userId") String userId,
            @Query("year") int year,
            @Query("month") int month,
            @Query("day") int day
    );

    // 캘린더 지출 날짜
    @GET("api/expenses/user/{userId}/range")
    Call<List<Records>> getMonthlyExpenseRecords(
            @Path("userId") String userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/expenses/user/{userId}/range")
    Call<List<Records>> getWeeklyExpenseRecords(
            @Path("userId") String userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/expenses/user/{userId}")
    Call<List<DetailRecords>> getDailyExpenseRecords(
            @Path("userId") String userId,
            @Query("year") int year,
            @Query("month") int month,
            @Query("day") int day
    );
}
