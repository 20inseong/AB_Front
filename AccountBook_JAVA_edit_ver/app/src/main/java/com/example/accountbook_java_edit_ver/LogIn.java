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

    private void LogInButton(){
        Button log_in_button = findViewById(R.id.login_button);
        EditText IDInput = findViewById(R.id.ID_input);
        EditText PWInput = findViewById(R.id.password_input);

        log_in_button.setOnClickListener(view -> {
            String IDstring = IDInput.getText().toString();
            String PWstring = PWInput.getText().toString();

            //서버에 값을 보내고, 돌아온 데이터를 통해 로그인을 하게 할 것인지 아닌지를 판별한다.

            Toast.makeText(LogIn.this,
                    "{\"유형\":\"" + IDstring + "\""+ PWstring +"\"",
                    Toast.LENGTH_SHORT).show();
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