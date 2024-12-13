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

            // Retrofit API 호출
            ApiService apiService = RetrofitClient.getClient("http://10.0.2.2:8080/").create(ApiService.class);
            Call<MemberResponse> call = apiService.getMemberById(IDstring);

            call.enqueue(new retrofit2.Callback<MemberResponse>() {
                @Override
                public void onResponse(Call<MemberResponse> call, retrofit2.Response<MemberResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MemberResponse member = response.body();

                        // 비밀번호 검증
                        if (member.getPassword().equals(PWstring)) {
                            Toast.makeText(LogIn.this, "로그인 성공: " + member.getName(), Toast.LENGTH_SHORT).show();

                            // 로그인 성공 후 MainActivity로 이동
                            Intent intent = new Intent(LogIn.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LogIn.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LogIn.this, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MemberResponse> call, Throwable t) {
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
}