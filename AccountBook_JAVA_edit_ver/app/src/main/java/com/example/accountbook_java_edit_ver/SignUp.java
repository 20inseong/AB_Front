package com.example.accountbook_java_edit_ver;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUp extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupButton();
    }

    private void signupButton(){
        Button signup_button = findViewById(R.id.signup_real_button);
        EditText et_real_name = findViewById(R.id.et_name);
        EditText et_username = findViewById(R.id.et_username);
        EditText et_password = findViewById(R.id.et_password);
        EditText et_confirm_password = findViewById(R.id.et_confirm_password);
        EditText et_nickname = findViewById(R.id.et_nickname);

        signup_button.setOnClickListener(view -> {
            String real_name = et_real_name.getText().toString();
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();
            String confirm_password = et_confirm_password.getText().toString();
            String nickname = et_nickname.getText().toString();

            // 비밀번호 확인
            if (password.equals(confirm_password)) {
                Toast.makeText(SignUp.this,
                        "회원가입 성공!\n이름: " + real_name + "\n아이디: " + username + "\n닉네임: " + nickname,
                        Toast.LENGTH_SHORT).show();
                finish();
                // 여기서 회원가입 처리를 수행 (e.g., 서버 요청 등)
            } else {
                Toast.makeText(SignUp.this,
                        "비밀번호가 일치하지 않습니다. 다시 확인해주세요.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
