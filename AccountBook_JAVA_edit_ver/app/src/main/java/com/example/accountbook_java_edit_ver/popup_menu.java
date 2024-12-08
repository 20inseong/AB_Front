package com.example.accountbook_java_edit_ver;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.PopupMenu;

public class popup_menu {

    private final Context context;

    // 생성자를 통해 Context 전달
    public popup_menu(Context context) {
        this.context = context;
    }

    // 메뉴 클릭 이벤트 처리
    public void handleMenuClick(MenuItem menuItem) {
        Intent intent = null;

        // 메뉴 항목 ID에 따라 화면 전환
        int itemId = menuItem.getItemId();
        if (itemId == R.id.menu_LogIn) {
            intent = new Intent(context, LogIn.class);
        }
        else if (itemId == R.id.menu_home) {
            intent = new Intent(context, MainActivity.class);
        }
        else if (itemId == R.id.menu_calendar) {
            intent = new Intent(context, CalendarPage.class);
        }
        else if (itemId == R.id.menu_fixed_expenses) {
            intent = new Intent(context, FixedExpenses.class);
        }
        else if (itemId == R.id.menu_inquiries) {
            intent = new Intent(context, Inquiry.class);
        }

        // Intent가 null이 아니면 화면 전환
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    // PopupMenu 설정 및 표시
    public void showPopupMenu(PopupMenu popupMenu) {
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            handleMenuClick(menuItem);
            return true;
        });

        popupMenu.show(); // 메뉴 표시
    }
}
