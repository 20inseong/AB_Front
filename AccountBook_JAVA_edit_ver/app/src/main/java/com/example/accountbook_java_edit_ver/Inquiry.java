package com.example.accountbook_java_edit_ver;

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

public class Inquiry extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private SidebarManager sidebarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FrameLayout에 자식 콘텐츠 추가
        FrameLayout frameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_inquiry, frameLayout, true);

        // Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.inquiry_layout);
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
        InquiryButton();
    }

    private void InquiryButton(){
        Button inquiry_button = findViewById(R.id.submit_button);
        EditText inquiry_things = findViewById(R.id.inquiry_content);
        EditText email_address = findViewById(R.id.email_input);

        inquiry_button.setOnClickListener(view -> {
            String inquiry_contents_string = inquiry_things.getText().toString();
            String email_string = email_address.getText().toString();
            String message = "{\"유형\":\"" + inquiry_contents_string + "\"" + email_string + "\"}";
            Toast.makeText(Inquiry.this, message, Toast.LENGTH_SHORT).show();
        });
    }

}
