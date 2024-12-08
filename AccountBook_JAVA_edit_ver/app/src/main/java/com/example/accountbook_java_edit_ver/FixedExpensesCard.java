package com.example.accountbook_java_edit_ver;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FixedExpensesCard extends LinearLayout {
    private TextView itemName;
    private CheckBox itemCheckbox;
    private LinearLayout detailsLayout;
    private TextView itemDescription;
    private TextView itemAmount;
    private TextView itemDate;
    private Button editButton;
    private Button deleteButton;

    // 카드 데이터를 저장하는 객체
    private fixedItem currentItem;
    private FixedExpensesCardListener listener;

    public FixedExpensesCard(Context context) {
        super(context);
        init(context);
    }

    // 리스너 설정 메서드
    public void setListener(FixedExpensesCardListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fixed_expenses_card, this, true);

        // XML 요소 초기화
        itemName = view.findViewById(R.id.item_name);
        itemCheckbox = view.findViewById(R.id.item_checkbox);
        detailsLayout = view.findViewById(R.id.details_layout);
        itemDescription = view.findViewById(R.id.item_description);
        itemAmount = view.findViewById(R.id.item_amount);
        itemDate = view.findViewById(R.id.item_date);
        editButton = view.findViewById(R.id.item_edit_button);
        deleteButton = view.findViewById(R.id.item_delete_button);

        // 카드 클릭 시 토글
        this.setOnClickListener(v -> toggleDetails());

        // 체크박스 상태 변경 리스너
        itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                itemName.setAlpha(1.0f); // 활성화 시 원래 상태
            } else {
                itemName.setAlpha(0.5f); // 비활성화 시 반투명
            }
        });

        // 편집 버튼 클릭 리스너
        editButton.setOnClickListener(v -> {
//            if(listener == null){
//                Toast.makeText(this.getContext(), "listener 실종 사건", Toast.LENGTH_SHORT).show();
//            }

            if (listener != null && currentItem != null) {
                listener.onEdit(currentItem);
            }
            else{
                Toast.makeText(this.getContext(), "편집 버튼 오류", Toast.LENGTH_SHORT).show();
            }
        });

        // 삭제 버튼 클릭 리스너
        deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(this);
            }
            else{
                Toast.makeText(this.getContext(), "삭제 버튼 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 세부 영역 토글
    private void toggleDetails() {
        if (detailsLayout.getVisibility() == View.GONE) {
            detailsLayout.setVisibility(View.VISIBLE);
        } else {
            detailsLayout.setVisibility(View.GONE);
        }
    }

    // 데이터 설정 메서드
    public void setData(String name, boolean isChecked, String description, int amount, String date) {
        try {
            // currentItem 초기화
            currentItem = new fixedItem(name, description, amount, isChecked, date);


            // UI 업데이트
            itemName.setText(name);
            itemCheckbox.setChecked(isChecked);
            itemDescription.setText(description);
            itemAmount.setText(String.format("₩%,d", amount)); // 포맷팅된 금액 표시
            itemDate.setText(date);

        } catch (NumberFormatException e) {
            // 예외 처리
            Toast.makeText(getContext(), "금액이 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            Log.e("FixedExpensesCard", "금액 변환 실패: ", e);
        }
    }


    public interface FixedExpensesCardListener {
        void onEdit(fixedItem item);
        void onDelete(FixedExpensesCard card);
    }
}
