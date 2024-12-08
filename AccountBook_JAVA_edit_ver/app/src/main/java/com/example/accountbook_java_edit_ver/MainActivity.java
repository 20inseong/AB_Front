package com.example.accountbook_java_edit_ver;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageButton menuButton;
    private SidebarManager sidebarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 메인 레이아웃 설정

        // FrameLayout에 자식 콘텐츠 추가
        FrameLayout frameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_main_content, frameLayout, true);

        // main_content 객체의 값 변형하기(예시 => 나중에 DB에 값 받기)
        View income_card = frameLayout.findViewById(R.id.in_card);
        TextView income_card_amount_view = income_card.findViewById(R.id.in_card_amount);
        income_card_amount_view.setText("0원");


        // 최근 기록 내역 컨테이너 참조
        LinearLayout recentRecordsContainer = frameLayout.findViewById(R.id.recent_records_container);

        // 동적으로 뷰를 추가할 데이터 리스트
        List<DetailRecords> recentRecords = getRecentRecords();

        // 동적으로 뷰 추가
        LayoutInflater inflater = LayoutInflater.from(this);

        for (DetailRecords record : recentRecords) {
            View recordView;

            if (record.isin1out0()) {
                // 수입 레이아웃 인플레이트
                recordView = inflater.inflate(R.layout.recent_income, recentRecordsContainer, false);
                // 수입 레이아웃의 뷰들 설정
                TextView dateView = recordView.findViewById(R.id.income_date);
                TextView amountView = recordView.findViewById(R.id.income_amount);
                TextView descriptionView = recordView.findViewById(R.id.income_description);
                dateView.setText(record.getDate());
                amountView.setText(record.getAmount());
                descriptionView.setText(record.getDescription());
            } else {
                // 지출 레이아웃 인플레이트
                recordView = inflater.inflate(R.layout.recent_spend, recentRecordsContainer, false);
                // 지출 레이아웃의 뷰들 설정
                TextView dateView = recordView.findViewById(R.id.spend_date);
                TextView amountView = recordView.findViewById(R.id.spend_amount);
                TextView descriptionView = recordView.findViewById(R.id.spend_description);
                dateView.setText(record.getDate());
                amountView.setText(record.getAmount());
                descriptionView.setText(record.getDescription());
            }

            // 레이아웃 매개변수 설정 (여기서는 마진만 설정)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, dpToPx(16), 0, 0); // 상단 마진 16dp
            recordView.setLayoutParams(params);

            // 컨테이너에 뷰 추가
            recentRecordsContainer.addView(recordView);
        }


        // Toolbar 설정
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.root_layout);
        navigationView = findViewById(R.id.navigation_view);

        // SidebarManager 초기화 및 설정
        sidebarManager = new SidebarManager(this, drawerLayout);
        sidebarManager.setupNavigationView(navigationView);

        // 메뉴 버튼 초기화 및 Drawer 열기 설정
        menuButton = findViewById(R.id.dropdown_button);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        // 팝업 기능 추가
        setupPopupButtons();
    }

    private void setupPopupButtons() {
        // 영수증 인식 버튼
        Button receiptButton = findViewById(R.id.receipt_button);
        receiptButton.setOnClickListener(view -> {
            // AlertDialog로 선택 팝업 표시
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("영수증 인식")
                    .setItems(new CharSequence[]{"카메라", "갤러리"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // 카메라 선택
                                Toast.makeText(MainActivity.this, "카메라 선택됨", Toast.LENGTH_SHORT).show();
                                break;
                            case 1: // 갤러리 선택
                                Toast.makeText(MainActivity.this, "갤러리 선택됨", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // 수동 추가 버튼
        // 수동 추가로 값을 입력하면 화면에 나오는 카드들 데이터가 변화할 수 있기 때문에 서버에서 값만 따로 바꿔야 할 듯 싶다.
        Button manualButton = findViewById(R.id.manual_button);
        manualButton.setOnClickListener(view -> {
            // 팝업 창 레이아웃 불러오기
            LayoutInflater inflater = getLayoutInflater();
            View popupView = inflater.inflate(R.layout.edit_input, null);

            // 팝업 내부 EditText 및 달력 관련 설정
            EditText dateEditText = popupView.findViewById(R.id.date_text);

            // 날짜 선택 이벤트 설정
            dateEditText.setOnClickListener(v -> {
                // 현재 날짜 가져오기
                Calendar calendar = Calendar.getInstance();

                // DatePickerDialog 띄우기
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view1, year, month, dayOfMonth) -> {
                            // 날짜 선택 후 EditText에 설정
                            String selectedDate = String.format(Locale.getDefault(), "%04d.%02d.%02d", year, month + 1, dayOfMonth);
                            dateEditText.setText(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                datePickerDialog.show();
            });

            // AlertDialog 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("수동 추가")
                    .setView(popupView)
                    .setPositiveButton("추가", (dialog, which) -> {
                        // 입력 값 가져오기
                        EditText amountInput = popupView.findViewById(R.id.input_amount);
                        EditText descriptionInput = popupView.findViewById(R.id.input_description);
                        Switch toggleIncomeExpense = popupView.findViewById(R.id.toggle_income_expense);

                        String amount = amountInput.getText().toString();
                        String description = descriptionInput.getText().toString();
                        String type = toggleIncomeExpense.isChecked() ? "수입" : "지출";

                        Toast.makeText(MainActivity.this,
                                "{\"유형\":\"" + type + "\""+ amount +"\"" + description + "\"}",
                                Toast.LENGTH_SHORT).show();

                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                    .show();

            // 토글 상태 변경 리스너 설정
            Switch toggleIncomeExpense = popupView.findViewById(R.id.toggle_income_expense);
            TextView toggleText = popupView.findViewById(R.id.toggle_text);

            // 기본값 설정
            toggleText.setText("지출");
            toggleIncomeExpense.setChecked(false);

            toggleIncomeExpense.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    toggleText.setText("수입");
                } else {
                    toggleText.setText("지출");
                }
            });
        });
    }

    // 예시 데이터를 가져오는 메서드
    private List<DetailRecords> getRecentRecords() {
        List<DetailRecords> records = new ArrayList<>();
        // 임의의 데이터 추가
        records.add(new DetailRecords(true, "100,000원", "월급", "24/11/02"));
        records.add(new DetailRecords(false, "50,000원", "식비","24/11/02"));
        records.add(new DetailRecords(false, "30,000원", "교통비","24/11/01"));
        records.add(new DetailRecords(false, "20,000원", "쇼핑","24/11/01"));
        records.add(new DetailRecords(true, "10,000원", "용돈","24/10/31"));
        return records;
    }

    // dp를 px로 변환하는 유틸리티 메서드
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
