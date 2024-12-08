package com.example.accountbook_java_edit_ver;

public class DetailRecords extends Records{
    private String description;

    public DetailRecords(boolean isIncome, String amount, String description, String date) {
        super(isIncome, amount, date);
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
