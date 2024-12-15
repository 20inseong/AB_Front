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

import com.google.android.material.navigation.NavigationView;;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private SidebarManager sidebarManager;
    private CalendarView calendarView;
//    private static final int REQUEST_IMAGE_CAPTURE = 672;
//    private String imageFilePath;
//    private Uri photoUri;
//    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 메인 레이아웃 설정

        String userId = SharedPreferencesUtils.getUserId(this); // 유틸리티 클래스 사용
        Toast.makeText(this, "사용자 id : " + userId, Toast.LENGTH_SHORT).show();

        if (userId == null) {
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // FrameLayout에 자식 콘텐츠 추가
            FrameLayout frameLayout = findViewById(R.id.content_frame);
            getLayoutInflater().inflate(R.layout.activity_calendar, frameLayout, true);

            calendarView = findViewById(R.id.calendarView);

            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                // 선택된 날짜를 설정
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);

                // 선택된 날짜에 맞는 데이터를 컨테이너에 업데이트
                updateRecords(userId, selectedDate);
            });

            // 오늘의 Date 정보를 서버로 보낸 후, 정보를 받아 함수를 적용한다.
            Calendar today = Calendar.getInstance();
            updateRecords(userId, today);
        }

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

        // 권한 체크
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            TedPermission.create()
//                    .setPermissionListener(permissionListener)
//                    .setRationaleMessage("카메라 권한이 필요합니다.")
//                    .setDeniedMessage("거부하셨습니다.")
//                    .setPermissions(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.CAMERA)
//                    .check();
//        } else {
//            TedPermission.create()
//                    .setPermissionListener(permissionListener)
//                    .setRationaleMessage("카메라 권한이 필요합니다.")
//                    .setDeniedMessage("거부하셨습니다.")
//                    .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
//                    .check();
//        }

        setupButtons();
    }

    private void updateRecords(String userId, Calendar selectedDate) {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH) + 1;
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        // 날짜 범위 계산
        Date[] monthDates = getMonthStartAndEndDate(selectedDate);
        Date[] weekDates = getWeekStartAndEndDate(selectedDate);

        String monthStartDate = formatDateToISO8601(monthDates[0]);
        String monthEndDate = formatDateToISO8601(monthDates[1]);
        String weekStartDate = formatDateToISO8601(weekDates[0]);
        String weekEndDate = formatDateToISO8601(weekDates[1]);

        ApiService apiService = RetrofitClient.getClient(ServerIP.SERVER_IP).create(ApiService.class);

        // 월별 수입 및 지출 데이터 가져오기
        Call<List<Records>> monthlyIncomeCall = apiService.getMonthlyIncomeRecords(userId, monthStartDate, monthEndDate);
        Call<List<Records>> monthlyExpenseCall = apiService.getMonthlyExpenseRecords(userId, monthStartDate, monthEndDate);


        monthlyIncomeCall.enqueue(new Callback<List<Records>>() {
            @Override
            public void onResponse(Call<List<Records>> call, Response<List<Records>> response) {
                if (response.isSuccessful()) {
                    List<Records> monthlyIncomeRecords = response.body();

                    if (monthlyIncomeRecords == null || monthlyIncomeRecords.isEmpty()) {
                        Toast.makeText(CalendarPage.this, "월별 수입 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        return; // 이후 로직 실행 방지
                    }

                    // 월별 지출 데이터 호출
                    monthlyExpenseCall.enqueue(new Callback<List<Records>>() {
                        @Override
                        public void onResponse(Call<List<Records>> call, Response<List<Records>> response) {
                            if (response.isSuccessful()) {
                                List<Records> monthlyExpenseRecords = response.body();

                                if (monthlyExpenseRecords == null || monthlyExpenseRecords.isEmpty()) {
                                    Toast.makeText(CalendarPage.this, "월별 지출 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 월별 합계 계산
                                int monthlyIncomeTotal = calculateTotalAmount(monthlyIncomeRecords, true);
                                int monthlyExpenseTotal = calculateTotalAmount(monthlyExpenseRecords, false);

                                // 월별 합계를 Toast로 출력
                                Toast.makeText(CalendarPage.this,
                                        "월별 수입 합계: " + monthlyIncomeTotal + "원\n" +
                                                "월별 지출 합계: " + monthlyExpenseTotal + "원",
                                        Toast.LENGTH_LONG).show();

                                // 주별 데이터 가져오기
                                fetchWeeklyData(apiService, userId, year, month, weekStartDate, weekEndDate, monthlyIncomeRecords, monthlyExpenseRecords, day);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Records>> call, Throwable t) {
                            handleFailure(t, "월별 지출 데이터 요청 오류");
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Records>> call, Throwable t) {
                handleFailure(t, "월별 수입 데이터 요청 오류");
            }
        });
    }

    // 주간 데이터 화면에 띄우기
    private void fetchWeeklyData(ApiService apiService, String userId, int year, int month, String weekStartDate, String weekEndDate,
                                 List<Records> monthlyIncomeRecords, List<Records> monthlyExpenseRecords, int day) {
        Call<List<Records>> weeklyIncomeCall = apiService.getWeeklyIncomeRecords(userId, weekStartDate, weekEndDate);
        Call<List<Records>> weeklyExpenseCall = apiService.getWeeklyExpenseRecords(userId, weekStartDate, weekEndDate);

        weeklyIncomeCall.enqueue(new Callback<List<Records>>() {
            @Override
            public void onResponse(Call<List<Records>> call, Response<List<Records>> response) {
                if (response.isSuccessful()) {
                    List<Records> weeklyIncomeRecords = response.body();

                    if (weeklyIncomeRecords == null || weeklyIncomeRecords.isEmpty()) {
                        Toast.makeText(CalendarPage.this, "주별 수입 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    weeklyExpenseCall.enqueue(new Callback<List<Records>>() {
                        @Override
                        public void onResponse(Call<List<Records>> call, Response<List<Records>> response) {
                            if (response.isSuccessful()) {
                                List<Records> weeklyExpenseRecords = response.body();

                                if (weeklyExpenseRecords == null || weeklyExpenseRecords.isEmpty()) {
                                    Toast.makeText(CalendarPage.this, "주별 지출 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 주별 합계 계산
                                int weeklyIncomeTotal = calculateTotalAmount(weeklyIncomeRecords, true);
                                int weeklyExpenseTotal = calculateTotalAmount(weeklyExpenseRecords, false);

                                // 주별 합계를 Toast로 출력
                                Toast.makeText(CalendarPage.this,
                                        "주별 수입 합계: " + weeklyIncomeTotal + "원\n" +
                                                "주별 지출 합계: " + weeklyExpenseTotal + "원",
                                        Toast.LENGTH_LONG).show();

                                // 일별 데이터 가져오기
                                fetchDailyData(apiService, userId, year, month, day, monthlyIncomeRecords, monthlyExpenseRecords, weeklyIncomeRecords, weeklyExpenseRecords);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Records>> call, Throwable t) {
                            handleFailure(t, "주별 지출 데이터 요청 오류");
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Records>> call, Throwable t) {
                handleFailure(t, "주별 수입 데이터 요청 오류");
            }
        });
    }


    // 당일 데이터 입력하기
    private void fetchDailyData(ApiService apiService, String userId, int year, int month, int day,
                                List<Records> monthlyIncomeRecords, List<Records> monthlyExpenseRecords,
                                List<Records> weeklyIncomeRecords, List<Records> weeklyExpenseRecords) {
        Call<List<DetailRecords>> dailyIncomeCall = apiService.getDailyIncomeRecords(userId, year, month, day);
        Call<List<DetailRecords>> dailyExpenseCall = apiService.getDailyExpenseRecords(userId, year, month, day);

        dailyIncomeCall.enqueue(new Callback<List<DetailRecords>>() {
            @Override
            public void onResponse(Call<List<DetailRecords>> call, Response<List<DetailRecords>> response) {
                if (response.isSuccessful()) {
                    List<DetailRecords> dailyIncomeRecords = response.body();

                    if (dailyIncomeRecords == null || dailyIncomeRecords.isEmpty()) {
                        Toast.makeText(CalendarPage.this, "당일 수입 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dailyExpenseCall.enqueue(new Callback<List<DetailRecords>>() {
                        @Override
                        public void onResponse(Call<List<DetailRecords>> call, Response<List<DetailRecords>> response) {
                            if (response.isSuccessful()) {
                                List<DetailRecords> dailyExpenseRecords = response.body();

                                if (dailyExpenseRecords == null || dailyExpenseRecords.isEmpty()) {
                                    Toast.makeText(CalendarPage.this, "당일 지출 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 컨테이너 갱신
                                refreshContainers(monthlyIncomeRecords, monthlyExpenseRecords,
                                        weeklyIncomeRecords, weeklyExpenseRecords,
                                        dailyIncomeRecords, dailyExpenseRecords);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<DetailRecords>> call, Throwable t) {
                            handleFailure(t, "당일 지출 데이터 요청 오류");
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<DetailRecords>> call, Throwable t) {
                handleFailure(t, "당일 수입 데이터 요청 오류");
            }
        });
    }


    // 새로고침 함수
    private void refreshContainers(List<Records> monthlyIncomeRecords, List<Records> monthlyExpenseRecords,
                                   List<Records> weeklyIncomeRecords, List<Records> weeklyExpenseRecords,
                                   List<DetailRecords> dailyIncomeRecords, List<DetailRecords> dailyExpenseRecords) {
        // 컨테이너 초기화
        LinearLayout monthlyContainer = findViewById(R.id.monthly_records_container);
        LinearLayout weeklyContainer = findViewById(R.id.weekly_records_container);
        LinearLayout dailyContainer = findViewById(R.id.today_records_container);

        monthlyContainer.removeAllViews();
        weeklyContainer.removeAllViews();
        dailyContainer.removeAllViews();

        // 월별 데이터 추가
        addMonthlyRecordsToContainer(monthlyContainer, monthlyIncomeRecords);
        addMonthlyRecordsToContainer(monthlyContainer, monthlyExpenseRecords);

        // 주별 데이터 추가
        addWeeklyRecordsToContainer(weeklyContainer, weeklyIncomeRecords);
        addWeeklyRecordsToContainer(weeklyContainer, weeklyExpenseRecords);

        // 일별 데이터 추가
        addDailyRecordsToContainer(dailyContainer, dailyIncomeRecords);
        addDailyRecordsToContainer(dailyContainer, dailyExpenseRecords);
    }


    private void addMonthlyRecordsToContainer(LinearLayout container, List<Records> records) {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Records Monthlyrecord : records) {
            View MonthlyrecordView;

            if (Monthlyrecord.isin1out0()) {
                // 수입 레이아웃 인플레이트
                MonthlyrecordView = inflater.inflate(R.layout.monthly_incard, container, false);
                // 수입 레이아웃의 뷰들 설정
                TextView dateView = MonthlyrecordView.findViewById(R.id.monthly_in_card_date);
                TextView amountView = MonthlyrecordView.findViewById(R.id.in_card_amount);
                dateView.setText(Monthlyrecord.getDate());
                amountView.setText(Monthlyrecord.getAmount());
            } else {
                // 지출 레이아웃 인플레이트
                MonthlyrecordView = inflater.inflate(R.layout.monthly_outcard, container, false);
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
            container.addView(MonthlyrecordView);
        }
    }

    private void addWeeklyRecordsToContainer(LinearLayout container, List<Records> records) {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Records Weeklyrecord : records) {
            View WeeklyrecordView;

            if (Weeklyrecord.isin1out0()) {
                // 수입 레이아웃 인플레이트
                WeeklyrecordView = inflater.inflate(R.layout.weekly_incard, container, false);
                // 수입 레이아웃의 뷰들 설정
                TextView dateView = WeeklyrecordView.findViewById(R.id.weekly_in_card_date);
                TextView amountView = WeeklyrecordView.findViewById(R.id.weekly_in_card_amount);
                dateView.setText(Weeklyrecord.getDate());
                amountView.setText(Weeklyrecord.getAmount());
            } else {
                // 지출 레이아웃 인플레이트
                WeeklyrecordView = inflater.inflate(R.layout.weekly_outcard, container, false);
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
            container.addView(WeeklyrecordView);
        }
    }


    private void addDailyRecordsToContainer(LinearLayout container, List<DetailRecords> records) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (DetailRecords Todayrecord : records) {
            View TodayrecordView;

            if (Todayrecord.isin1out0()) {
                // 수입 레이아웃 인플레이트
                TodayrecordView = inflater.inflate(R.layout.today_income, container, false);
                // 수입 레이아웃의 뷰들 설정
                TextView amountView = TodayrecordView.findViewById(R.id.today_in_card_amount);
                TextView descriptionView = TodayrecordView.findViewById(R.id.today_income_description);
                amountView.setText(Todayrecord.getAmount());
                descriptionView.setText(Todayrecord.getDescription());
            } else {
                // 지출 레이아웃 인플레이트
                TodayrecordView = inflater.inflate(R.layout.today_spend, container, false);
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
            container.addView(TodayrecordView);
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
//                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                if (intent.resolveActivity(getPackageManager()) != null) {
//                                    File photoFile = null;
//                                    try {
//                                        photoFile = createImageFile();
//                                    } catch (IOException e) {
//
//                                    }
//
//                                    if (photoFile != null) {
//                                        photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
//                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//                                    }
//                                }
                                Toast.makeText(CalendarPage.this, "카메라 선택됨", Toast.LENGTH_SHORT).show();
                                break;
                            case 1: // 갤러리 선택
//                                galleryLauncher.launch("image/*");
//
//                                galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
//                                        uri -> {
//                                            if (uri != null) {
//                                                // 선택된 이미지 처리
//                                                handleSelectedImage(uri);
//                                            }
//                                        });
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

//    // 만약 Month와 Week 데이터 계산을 여기서 해야한다면 따로 자동 계산식을 만들어야 한다.
//    // Month 예시 데이터를 가져오는 메서드
//    private List<Records> getMonthlyRecentRecords(int year, int month, int input_month, int output_month) {
//        List<Records> records = new ArrayList<>();
//        // 임의의 데이터 추가
//        records.add(new Records(true, input_month + "원", year + "년 " + month +"월"));
//        records.add(new Records(false, output_month + "원",year + "년" + month +"월"));
//        return records;
//    }
//
//
//    // Week 예시 데이터를 가져오는 메서드
//    private List<Records> getWeeklyRecentRecords(int year, int weekOfYear, int input_week, int output_week) {
//        List<Records> records = new ArrayList<>();
//        // 임의의 데이터 추가
//        records.add(new Records(true, input_week + "원",year + "년 " + weekOfYear +"주"));
//        records.add(new Records(false, output_week + "원",year + "년 " + weekOfYear +"주"));
//        return records;
//    }
//
//    // Day 예시 데이터를 가져오는 메서드
//    private List<DetailRecords> getTodayRecentRecords(int year, int month, int dayOfToday) {
//        List<DetailRecords> records = new ArrayList<>();
//        // 임의의 데이터 추가
//        records.add(new DetailRecords(true, "100,000원", "월급", "날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
//        records.add(new DetailRecords(false, "50,000원", "식비","날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
//        records.add(new DetailRecords(false, "30,000원", "교통비","날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
//        records.add(new DetailRecords(false, "20,000원", "쇼핑","날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
//        records.add(new DetailRecords(true, "10,000원", "용돈","날짜: " + year + "년 " + month + "월 " + dayOfToday + "일"));
//        return records;
//    }

    // dp를 px로 변환하는 유틸리티 메서드
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private Date[] getMonthStartAndEndDate(Calendar date) {
        Calendar startDate = (Calendar) date.clone();
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);

        Calendar endDate = (Calendar) startDate.clone();
        endDate.add(Calendar.MONTH, 1);
        endDate.add(Calendar.DAY_OF_MONTH, -1);
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);

        return new Date[]{startDate.getTime(), endDate.getTime()};
    }

    private Date[] getWeekStartAndEndDate(Calendar date) {
        Calendar startDate = (Calendar) date.clone();
        startDate.setFirstDayOfWeek(Calendar.MONDAY);
        startDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);

        Calendar endDate = (Calendar) startDate.clone();
        endDate.add(Calendar.DAY_OF_WEEK, 6);
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);

        return new Date[]{startDate.getTime(), endDate.getTime()};
    }


    private String formatDateToISO8601(Date date) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        return isoFormat.format(date);
    }

    private int calculateTotalAmount(List<? extends Records> records, boolean isIncome) {
        int total = 0;
        for (Records record : records) {
            if (record.isin1out0() == isIncome) {
                total += Integer.parseInt(record.getAmount().replaceAll("[^0-9]", ""));
            }
        }
        return total;
    }

    private void handleFailure(Throwable t, String errorMessage) {
        Toast.makeText(CalendarPage.this, errorMessage, Toast.LENGTH_SHORT).show();
        t.printStackTrace();
    }

//    private void handleSelectedImage(Uri uri) {
//        // 선택된 이미지 화면에 표시
//        //((ImageView) findViewById(R.id.imageView)).setImageURI(uri);
//
//        // 이미지를 비트맵으로 변환
//        Bitmap bitmap = null;
//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        if (bitmap != null) {
//            // OCR 처리 등 추가 작업
//            new UploadImageTask().execute(bitmap);
//        }
//    }
//
//    private File createImageFile() throws IOException {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "TEST_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,
//                ".jpeg",
//                storageDir
//        );
//        imageFilePath = image.getAbsolutePath();
//        return image;
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
//            ExifInterface exif = null;
//
//            try {
//                exif = new ExifInterface(imageFilePath);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            int exifOrientation;
//            int exifDegree;
//
//            if (exif != null) {
//                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//                exifDegree = exifOrientationToDegress(exifOrientation);
//            } else {
//                exifDegree = 0;
//            }
//
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
//            Date curDate = new Date(System.currentTimeMillis());
//            String filename = formatter.format(curDate);
//
//            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "MEDIKOK" + File.separator;
//            File file = new File(strFolderName);
//            if (!file.exists())
//                file.mkdirs();
//
//            File f = new File(strFolderName + "/" + filename + ".jpeg");
//
//            FileOutputStream fOut = null;
//            try {
//                fOut = new FileOutputStream(f);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            Bitmap bitImage;
//            bitImage = rotate(bitmap, exifDegree);
//            //이미지 저장
//            bitImage.compress(Bitmap.CompressFormat.JPEG, 70, fOut);
//
//            try {
//                fOut.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                fOut.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            //화면에 찍은 이미지 표시
//            //((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitImage);
//            MediaScannerConnection.scanFile(context, new String[]{f.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
//                @Override
//                public void onScanCompleted(String path, Uri uri) {
//                    Log.i("ExternalStorage", "Scanned " + path + ":");
//                    Log.i("ExternalStorage", "-> uri=" + uri);
//                }
//            });
//            new UploadImageTask().execute(bitImage);
//        }
//    }
//
//    private int exifOrientationToDegress(int exifOrientation) {
//        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
//            return 90;
//        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
//            return 180;
//        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
//            return 270;
//        }
//        return 0;
//    }
//
//    private Bitmap rotate(Bitmap bitmap, float degree) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(degree);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }
//
//    PermissionListener permissionListener = new PermissionListener() {
//        @Override
//        public void onPermissionGranted() {
//            Toast.makeText(getApplicationContext(), "권한이 허용됨", Toast.LENGTH_SHORT).show();
//        }
//        @Override
//        public void onPermissionDenied(List<String> List) {
//            Toast.makeText(getApplicationContext(), "권한이 거부됨", Toast.LENGTH_SHORT).show();
//        }
//    };
//
//    private static class UploadImageTask extends AsyncTask<Bitmap, Void, String> {
//        @Override
//        protected String doInBackground(Bitmap... bitmaps) {
//            Bitmap bitmap = bitmaps[0];
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//            byte[] imageBytes = byteArrayOutputStream.toByteArray();
//            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
//
//            String user_id = "parksuga2001";    //예시 id, 여기에 로그인 시 사용한 아이디 들어갈 예정, 일단 고정 값
//            String server_ip = "http://" + ServerIP.SERVER_IP + ":8080/api/ocr/process";
//
//
//            // 스프링 부트 서버로 이미지를 전송하는 코드 작성
//            try {
//                //여기서 스프링 부트 서버의 saveImage로 경로를 정해준 것임, 이건 spring boot 서버 완성하면 바뀔 예정
//                URL url = new URL(server_ip);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                Log.d("AddImage", "URL 이용");
//                conn.setRequestMethod("POST");  //POST형식
//                conn.setRequestProperty("Content-Type", "application/json; utf-8");
//                conn.setRequestProperty("Accept", "application/json");
//                conn.setDoOutput(true);
//
//                //이게 보내는 내용 : \"image\":\"" + base64Image
//                //String 형식으로 key + 데이터 조합 맞춰서 보내면 됩니다 :)
//                //쭉 이어붙여도, backend에서 key로 구분할 수 있어서, 필요한 데이터 저런식으로 보내면 됨.
//                String jsonInputString = "{\"image\":\"" + base64Image + "\", \"user_id\":\"" + user_id + "\"}";
//
//                //서버로 전송
//                try (OutputStream os = conn.getOutputStream()) {
//                    byte[] input = jsonInputString.getBytes("utf-8");
//                    os.write(input, 0, input.length);
//                }
//
//                int code = conn.getResponseCode();
//                if (code == 200) {
//                    Log.d("AddImage", "성공");
//                    return "Image uploaded successfully";
//                } else {
//                    Log.d("AddImage", code + " : 실패 코드");
//                    return "Failed to upload image: " + code;
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.d("AddImage", "오류");
//                return "Exception: " + e.getMessage();
//            }
//        }
//    }
}