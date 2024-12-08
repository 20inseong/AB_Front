package com.example.accountbook_java_edit_ver;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class SidebarManager {

    private final Context context;
    private final DrawerLayout drawerLayout;

    public SidebarManager(Context context, DrawerLayout drawerLayout) {
        this.context = context;
        this.drawerLayout = drawerLayout;
    }

    // 사이드바 메뉴 항목 클릭 이벤트 처리
    public void setupNavigationView(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            handleMenuClick(menuItem);
            drawerLayout.closeDrawer(GravityCompat.START); // 사이드바 닫기
            return true;
        });
    }

    // 메뉴 클릭 이벤트 처리
    private void handleMenuClick(MenuItem menuItem) {
        Intent intent = null;

        // 메뉴 항목 ID에 따라 액티비티 전환
        int itemId = menuItem.getItemId();
        if (itemId == R.id.menu_LogIn) {
            intent = new Intent(context, LogIn.class);
        } else if (itemId == R.id.menu_home) {
            intent = new Intent(context, MainActivity.class);
        } else if(itemId == R.id.menu_calendar) {
            intent = new Intent(context, CalendarPage.class);
        } else if(itemId == R.id.menu_fixed_expenses) {
            intent = new Intent(context, FixedExpenses.class);
        } else if (itemId == R.id.menu_inquiries) {
            intent = new Intent(context, Inquiry.class);
        }

        if (intent != null) {
            context.startActivity(intent);
        }
    }
}
