package com.example.accountbook_java_edit_ver;

import static android.os.Environment.DIRECTORY_PICTURES;
import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.material.navigation.NavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
