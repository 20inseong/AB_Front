package com.example.accountbook_java_edit_ver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class FixedExpenses extends AppCompatActivity implements FixedExpensesCard.FixedExpensesCardListener {
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
        getLayoutInflater().inflate(R.layout.activity_fixed_expenses, frameLayout, true);

        // Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.fixedexpenses_layout);
        navigationView = findViewById(R.id.navigation_view);

        // SidebarManager 초기화 및 설정
        sidebarManager = new SidebarManager(this, drawerLayout);
        sidebarManager.setupNavigationView(navigationView);

        // 메뉴 버튼 초기화 및 Drawer 열기 설정
        menuButton = findViewById(R.id.dropdown_button);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        refreshItem();
        addBtn();
    }

    @Override
    public void onEdit(fixedItem item) {
        // 편집 팝업 표시
        showEditPopup("편집", item);
    }

    @Override
    public void onDelete(FixedExpensesCard card) {
        // 카드 삭제
        LinearLayout parent = (LinearLayout) card.getParent();
        if (parent != null) {
            parent.removeView(card);
            Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshItem() {
        // 지출 비용, 수입 비용 객체 입력
        List<fixedItem> incomeItems = getIncomeItems();
        List<fixedItem> spendItems = getExpenseItems();

        LinearLayout incomeContainer = findViewById(R.id.incontainer);
        LinearLayout spendContainer = findViewById(R.id.outcontainer);

        if (incomeContainer == null || spendContainer == null) {
            Toast.makeText(this, "컨테이너를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        incomeContainer.removeAllViews();
        spendContainer.removeAllViews();

        // FixedExpensesCard 사용
        addItems(incomeContainer, incomeItems);
        addItems(spendContainer, spendItems);
    }

    private void addItems(LinearLayout container, List<fixedItem> items) {
        for (fixedItem item : items) {
            FixedExpensesCard card = new FixedExpensesCard(this);
            card.setListener(this);
            card.setData(item.getName(), item.isCheckOrNot(), item.getDescription(), item.getAmount(), item.getDate());
            container.addView(card);
        }
    }

    private void addBtn() {
        // 지출 버튼
        Button spendBtn = findViewById(R.id.fixed_out_plus_btn);
        spendBtn.setOnClickListener(view -> showPopup("고정 지출", false));

        // 수입 버튼
        Button incomeBtn = findViewById(R.id.fixed_in_plus_btn);
        incomeBtn.setOnClickListener(view -> showPopup("고정 수입", true));
    }

    private void showPopup(String title, boolean isIncome) {
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.fixed_input_thing, null);

        // Spinner 설정
        Spinner popupSpinner = popupView.findViewById(R.id.date_input);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.my_array, // strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupSpinner.setAdapter(adapter);



        // AlertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(FixedExpenses.this);
        builder.setTitle(title)
                .setView(popupView)
                .setPositiveButton("저장", (dialog, which) -> {
                    // 입력 데이터 가져오기
                    EditText titleNameInput = popupView.findViewById(R.id.fixed_title_name);
                    EditText amountInput = popupView.findViewById(R.id.fixed_amount);
                    EditText descriptionInput = popupView.findViewById(R.id.fixed_description);
                    String selectedDate = popupSpinner.getSelectedItem().toString();

                    String amountStr = amountInput.getText().toString();
                    String description = descriptionInput.getText().toString();
                    String titleName = titleNameInput.getText().toString();

                    if (amountStr.isEmpty() || description.isEmpty() || selectedDate.isEmpty()) {
                        Toast.makeText(FixedExpenses.this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        int amount = Integer.parseInt(amountStr);

                        // 새 데이터 생성
                        fixedItem newItem = new fixedItem(titleName, description, amount, false, selectedDate);

                        // 컨테이너 결정 및 추가
                        if (isIncome) {
                            addCardToContainer(findViewById(R.id.incontainer), newItem);
                        } else {
                            addCardToContainer(findViewById(R.id.outcontainer), newItem);
                        }

                        Toast.makeText(FixedExpenses.this,
                                String.format("추가 완료: 금액=%s, 설명=%s, 날짜=%s", titleName, amountStr, description, selectedDate),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addCardToContainer(LinearLayout container, fixedItem item) {
        FixedExpensesCard card = new FixedExpensesCard(this);
        card.setListener(this);
//        Toast.makeText(FixedExpenses.this, "listener 실종 사건", Toast.LENGTH_SHORT).show();
        card.setData(
                item.getName(),
                item.isCheckOrNot(),
                item.getDescription(),
                item.getAmount(),
                item.getDate()
        );
        container.addView(card);
    }

    private void showEditPopup(String title, fixedItem item) {
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.fixed_input_thing, null);

        // 스피너 설정
        Spinner popupSpinner = popupView.findViewById(R.id.date_input);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.my_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupSpinner.setAdapter(adapter);

        // 기존 데이터 로드
        EditText titleNameInput = popupView.findViewById(R.id.fixed_title_name);
        EditText amountInput = popupView.findViewById(R.id.fixed_amount);
        EditText descriptionInput = popupView.findViewById(R.id.fixed_description);

        titleNameInput.setText(item.getName());
        amountInput.setText(String.valueOf(item.getAmount()));
        descriptionInput.setText(item.getDescription());
        popupSpinner.setSelection(adapter.getPosition(item.getDate()));

        // AlertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(FixedExpenses.this);
        builder.setTitle(title)
                .setView(popupView)
                .setPositiveButton("저장", (dialog, which) -> {
                    // 수정된 데이터 가져오기
                    String newTitleName = titleNameInput.getText().toString();
                    String newAmountStr = amountInput.getText().toString();
                    String newDescription = descriptionInput.getText().toString();
                    String newSelectedDate = popupSpinner.getSelectedItem().toString();

                    if (newTitleName.isEmpty() || newAmountStr.isEmpty() || newDescription.isEmpty() || newSelectedDate.isEmpty()) {
                        Toast.makeText(FixedExpenses.this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        int newAmount = Integer.parseInt(newAmountStr);

                        // 기존 아이템 수정
                        item.setName(newTitleName);
                        item.setAmount(newAmount);
                        item.setDescription(newDescription);
                        item.setDate(newSelectedDate);
                        item.setCheckOrNot(true);

                        refreshItem(); // UI 업데이트
                        Toast.makeText(FixedExpenses.this, "수정 완료!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private List<fixedItem> getExpenseItems() {
        List<fixedItem> items = new ArrayList<>();
        items.add(new fixedItem("Netflix", "OTT 서비스", 15000, true, "1일"));
        items.add(new fixedItem("Disney+", "OTT 서비스", 12000, false, "2일"));
        return items;
    }

    private List<fixedItem> getIncomeItems() {
        List<fixedItem> items = new ArrayList<>();
        items.add(new fixedItem("월급", "정기 수입", 3000000, true, "3일"));
        items.add(new fixedItem("부수입", "추가 수익", 500000, false, "4일"));
        return items;
    }
}
