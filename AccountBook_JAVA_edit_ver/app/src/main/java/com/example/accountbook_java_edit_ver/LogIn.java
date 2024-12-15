package com.example.accountbook_java_edit_ver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.http.*;

public class LogIn extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private SidebarManager sidebarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 메인 레이아웃 설정

        // FrameLayout에 자식 콘텐츠 추가
        FrameLayout frameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_log_in, frameLayout, true);

        // Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.login_layout);
        navigationView = findViewById(R.id.navigation_view);

        // SidebarManager 초기화 및 설정
        sidebarManager = new SidebarManager(this, drawerLayout);
        sidebarManager.setupNavigationView(navigationView);


        // 메뉴 버튼 초기화 및 Drawer 열기 설정
        menuButton = findViewById(R.id.dropdown_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        LogInButton();
        SignUpButton();
    }

    private void LogInButton() {
        Button log_in_button = findViewById(R.id.login_button);
        EditText IDInput = findViewById(R.id.ID_input);
        EditText PWInput = findViewById(R.id.password_input);

        log_in_button.setOnClickListener(view -> {
            String IDstring = IDInput.getText().toString();
            String PWstring = PWInput.getText().toString();

            if (IDstring.isEmpty() || PWstring.isEmpty()) {
                Toast.makeText(LogIn.this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrofit API 호출
            ApiService apiService = RetrofitClient.getClient(ServerIP.SERVER_IP).create(ApiService.class);
            LoginRequest loginRequest = new LoginRequest(IDstring, PWstring);

            Call<String> call = apiService.login(loginRequest);

            call.enqueue(new retrofit2.Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String token = response.body(); // JWT 토큰 받기
                        Toast.makeText(LogIn.this, "로그인 성공: " + token, Toast.LENGTH_SHORT).show();

                        // 로그인 ID 저장 (토큰이 필요할 경우 저장 가능)
                        saveUserId(IDstring);

                        // 토큰 저장 (옵션)
                        saveToken(token);

                        // MainActivity로 이동
                        Intent intent = new Intent(LogIn.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LogIn.this, "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(LogIn.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    private void SignUpButton(){
        Button sign_up_button = findViewById(R.id.signup_button);

        sign_up_button.setOnClickListener(view -> {
            Intent intent = new Intent(LogIn.this, SignUp.class);
            startActivity(intent);
        });
    }

    private void saveUserId(String userId) {
        SharedPreferencesUtils.saveUserId(this, userId); // 유틸리티 클래스 사용
    }

    // JWT 토큰 저장 메서드
    private void saveToken(String token) {
        SharedPreferencesUtils.saveToken(this, token); // 토큰 저장 유틸리티 사용
    }

}