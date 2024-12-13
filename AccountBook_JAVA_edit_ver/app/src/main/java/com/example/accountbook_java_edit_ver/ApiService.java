package com.example.accountbook_java_edit_ver;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // 새로운 로그인 API 정의
    @GET("members/{userId}")
    Call<MemberResponse> getMemberById(@Path("userId") String userId);

    // 회원가입 API 정의
    @POST("members/register")  // Swagger 문서를 참고하여 경로 설정
    Call<Void> signUp(@Body MemberRequest memberRequest);

//    // 캘린더 날짜
//    @GET("api/records/monthly")
//    Call<List<ExpenseDTO>> getMonthlyRecords(
//            @Query("year") int year,
//            @Query("month") int month
//    );
//
//    @GET("api/records/weekly")
//    Call<List<ExpenseDTO>> getWeeklyRecords(
//            @Query("year") int year,
//            @Query("weekOfYear") int weekOfYear
//    );
//
//    @GET("api/records/daily")
//    Call<List<ExpenseDTO>> getDailyRecords(
//            @Query("year") int year,
//            @Query("month") int month,
//            @Query("day") int day
//    );
}
