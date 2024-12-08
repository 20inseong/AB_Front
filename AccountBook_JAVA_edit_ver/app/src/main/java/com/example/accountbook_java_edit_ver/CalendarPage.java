package com.example.accountbook_java_edit_ver;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private SidebarManager sidebarManager;
    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 메인 레이아웃 설정

        // FrameLayout에 자식 콘텐츠 추가
        FrameLayout frameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_calendar, frameLayout, true);

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // 선택된 날짜를 설정
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);

            // 선택된 날짜에 맞는 데이터를 컨테이너에 업데이트
            updateRecords(selectedDate);
        });

        // 오늘의 Date 정보를 서버로 보낸 후, 정보를 받아 함수를 적용한다.
        Calendar today = Calendar.getInstance();
        updateRecords(today);


        // Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.calendar_layout);
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

        setupButtons();
    }

    private void updateRecords(Calendar selectedDate) {
        // 날짜 정보 계산
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH) + 1; // 1월이 0부터 시작
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);
        int weekOfYear = selectedDate.get(Calendar.WEEK_OF_YEAR);

        // 컨테이너 업데이트
        refreshContainers(year, month, weekOfYear, day);

        // 선택된 날짜 정보를 Toast로 출력 (디버깅용)
        String message = String.format(Locale.getDefault(),
                "선택한 날짜: %04d-%02d-%02d\n주: %d주", year, month, day, weekOfYear);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void refreshContainers(int year, int month, int weekOfYear, int day) {
        // 컨테이너 참조
        LinearLayout MonthlyRecordsContainer = findViewById(R.id.monthly_records_container);
        LinearLayout WeeklyRecordsContainer = findViewById(R.id.weekly_records_container);
        LinearLayout TodayRecordsContainer = findViewById(R.id.today_records_container);

        // 데이터 가져오기
        List<Records> monthlyRecords = getMonthlyRecentRecords(year, month, 2000000, 150000);
        List<Records> weeklyRecords = getWeeklyRecentRecords(year, weekOfYear, 0, 15000);
        List<DetailRecords> todayRecords = getTodayRecentRecords(year, month, day);

        // 컨테이너 초기화
        MonthlyRecordsContainer.removeAllViews();
        WeeklyRecordsContainer.removeAllViews();
        TodayRecordsContainer.removeAllViews();

        // 데이터 추가
        addAllRecords(MonthlyRecordsContainer, WeeklyRecordsContainer, TodayRecordsContainer, monthlyRecords, weeklyRecords, todayRecords);

    }

    private void addAllRecords(LinearLayout MonthlyRecordsContainer,
                               LinearLayout WeeklyRecordsContainer,
                               LinearLayout TodayRecordsContainer,
                               List<Records> monthlyRecords,
                               List<Records> weeklyRecords,
                               List<DetailRecords> todayRecords){
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Records Monthlyrecord : monthlyRecords) {
            View MonthlyrecordView;

            if (Monthlyrecord.isin1out0()) {
                // 수입 레이아웃 인플레이트
                MonthlyrecordView = inflater.inflate(R.layout.monthly_incard, MonthlyRecordsContainer, false);
                // 수입 레이아웃의 뷰들 설정
                TextView dateView = MonthlyrecordView.findViewById(R.id.monthly_in_card_date);
                TextView amountView = MonthlyrecordView.findViewById(R.id.in_card_amount);
                dateView.setText(Monthlyrecord.getDate());
                amountView.setText(Monthlyrecord.getAmount());
            } else {
                // 지출 레이아웃 인플레이트
                MonthlyrecordView = inflater.inflate(R.layout.monthly_outcard, MonthlyRecordsContainer, false);
                // 지출 레이아웃의 뷰들 설정
                TextView dateView = MonthlyrecordView.findViewById(R.id.monthly_out_card_date);
                TextView amountView = MonthlyrecordView.findViewById(R.id.out_card_amount);
                dateView.setText(Monthlyrecord.getDate());
                amountView.setText(Monthlyrecord.getAmount());

            }

            // 레이아웃 매개변수 설정 (여기서는 마진만 설정)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, dpToPx(16), 0, 0); // 상단 마진 16dp
            MonthlyrecordView.setLayoutParams(params);

            // 컨테이너에 뷰 추가
            MonthlyRecordsContainer.addView(MonthlyrecordView);
        }



        for (Records Weeklyrecord : weeklyRecords) {
            View WeeklyrecordView;

            if (Weeklyrecord.isin1out0()) {
                // 수입 레이아웃 인플레이트
                WeeklyrecordView = inflater.inflate(R.layout.weekly_incard, WeeklyRecordsContainer, false);
                // 수입 레이아웃의 뷰들 설정
                TextView dateView = WeeklyrecordView.findViewById(R.id.weekly_in_card_date);
                TextView amountView = WeeklyrecordView.findViewById(R.id.weekly_in_card_amount);
                dateView.setText(Weeklyrecord.getDate());
                amountView.setText(Weeklyrecord.getAmount());
            } else {
                // 지출 레이아웃 인플레이트
                WeeklyrecordView = inflater.inflate(R.layout.weekly_outcard, WeeklyRecordsContainer, false);
                // 지출 레이아웃의 뷰들 설정
                TextView dateView = WeeklyrecordView.findViewById(R.id.weekly_out_card_date);
                TextView amountView = WeeklyrecordView.findViewById(R.id.weekly_out_card_amount);
                dateView.setText(Weeklyrecord.getDate());
                amountView.setText(Weeklyrecord.getAmount());

            }

            // 레이아웃 매개변수 설정 (여기서는 마진만 설정)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, dpToPx(16), 0, 0); // 상단 마진 16dp
            WeeklyrecordView.setLayoutParams(params);

            // 컨테이너에 뷰 추가
            WeeklyRecordsContainer.addView(WeeklyrecordView);
        }


        for (DetailRecords Todayrecord : todayRecords) {
            View TodayrecordView;

            if (Todayrecord.isin1out0()) {
                // 수입 레이아웃 인플레이트
                TodayrecordView = inflater.inflate(R.layout.today_income, TodayRecordsContainer, false);
                // 수입 레이아웃의 뷰들 설정
                TextView amountView = TodayrecordView.findViewById(R.id.today_in_card_amount);
                TextView descriptionView = TodayrecordView.findViewById(R.id.today_income_description);
                amountView.setText(Todayrecord.getAmount());
                descriptionView.setText(Todayrecord.getDescription());
            } else {
                // 지출 레이아웃 인플레이트
                TodayrecordView = inflater.inflate(R.layout.today_spend, TodayRecordsContainer, false);
                // 지출 레이아웃의 뷰들 설정
                TextView amountView = TodayrecordView.findViewById(R.id.today_out_card_amount);
                TextView descriptionView = TodayrecordView.findViewById(R.id.today_income_description);

                amountView.setText(Todayrecord.getAmount());
                descriptionView.setText(Todayrecord.getDescription());
            }

            // 레이아웃 매개변수 설정 (여기서는 마진만 설정)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, dpToPx(16), 0, 0); // 상단 마진 16dp
            TodayrecordView.setLayoutParams(params);

            // 컨테이너에 뷰 추가
            TodayRecordsContainer.addView(TodayrecordView);
        }

    }


    private void setupButtons() {
        // 영수증 인식 버튼
        Button receiptButton = findViewById(R.id.receipt_button);
        receiptButton.setOnClickListener(view -> {
            // AlertDialog로 선택 팝업 표시
            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarPage.this);
            builder.setTitle("영수증 인식")
                    .setItems(new CharSequence[]{"카메라", "갤러리"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // 카메라 선택
                                Toast.makeText(CalendarPage.this, "카메라 선택됨", Toast.LENGTH_SHORT).show();
                                break;
                            case 1: // 갤러리 선택
                                Toast.makeText(CalendarPage.this, "갤러리 선택됨", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // 수동 추가 버튼
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
                        CalendarPage.this,
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
            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarPage.this);
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

                        Toast.makeText(CalendarPage.this,
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

    // 만약 Month와 Week 데이터 계산을 여기서 해야한다면 따로 자동 계산식을 만들어야 한다.
    // Month 예시 데이터를 가져오는 메서드
    private List<Records> getMonthlyRecentRecords(int year, int month, int input_month, int output_month) {
        List<Records> records = new ArrayList<>();
        // 임의의 데이터 추가
        records.add(new Records(true, input_month + "원", year + "년 " + month +"월"));
        records.add(new Records(false, output_month + "원",year + "년" + month +"월"));
        return records;
    }


    // Week 예시 데이터를 가져오는 메서드
    private List<Records> getWeeklyRecentRecords(int year, int weekOfYear, int input_week, int output_week) {
        List<Records> records = new ArrayList<>();
        // 임의의 데이터 추가
        records.add(new Records(true, input_week + "원",year + "년 " + weekOfYear +"주"));
        records.add(new Records(false, output_week + "원",year + "년 " + weekOfYear +"주"));
        return records;
    }

    // Day 예시 데이터를 가져오는 메서드
    private List<DetailRecords> getTodayRecentRecords(int year, int month, int dayOfToday) {
        List<DetailRecords> records = new ArrayList<>();
        // 임의의 데이터 추가
        records.add(new DetailRecords(true, "100,000원", "월급", "날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
        records.add(new DetailRecords(false, "50,000원", "식비","날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
        records.add(new DetailRecords(false, "30,000원", "교통비","날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
        records.add(new DetailRecords(false, "20,000원", "쇼핑","날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
        records.add(new DetailRecords(true, "10,000원", "용돈","날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
        return records;
    }

    // dp를 px로 변환하는 유틸리티 메서드
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
