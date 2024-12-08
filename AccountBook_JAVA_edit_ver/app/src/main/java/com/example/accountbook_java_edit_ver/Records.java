package com.example.accountbook_java_edit_ver;

// Record 클래스 정의
public class Records {
    private boolean in1out0;
    private String amount;
    private String date;

    public Records(boolean isIncome, String amount, String date) {
        this.in1out0 = isIncome;
        this.amount = amount;
        this.date = date;
    }

    public boolean isin1out0() {
        return in1out0;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}