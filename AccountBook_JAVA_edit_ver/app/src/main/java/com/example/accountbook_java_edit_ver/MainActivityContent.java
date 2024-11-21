package com.example.accountbook_java_edit_ver;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivityContent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);

        // 지출 카드의 텍스트 변경
        TextView outCardAmount = findViewById(R.id.out_card_amount);
        outCardAmount.setText("0원");

        // 수입 카드의 텍스트 변경
        TextView inCardAmount = findViewById(R.id.in_card_amount);
        inCardAmount.setText("0원");

        // 왜 바뀌지 않는거지...?
    }
}
