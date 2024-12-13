package com.example.accountbook_java_edit_ver;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        EditText et_email = findViewById(R.id.et_email);

        signup_button.setOnClickListener(view -> {
            String real_name = et_real_name.getText().toString();
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();
            String confirm_password = et_confirm_password.getText().toString();
            String email = et_email.getText().toString();

            // 비밀번호 확인
            if (password.equals(confirm_password)) {
                // Retrofit API 호출
                MemberRequest memberRequest = new MemberRequest(real_name, username, password, email);
                ApiService apiService = RetrofitClient.getClient("http://10.0.2.2:8080/").create(ApiService.class);

                Call<Void> call = apiService.signUp(memberRequest);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUp.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                            finish(); // 회원가입 성공 후 Activity 종료
                        } else {
                            Toast.makeText(SignUp.this, "회원가입 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(SignUp.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(SignUp.this, "비밀번호가 일치하지 않습니다. 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
