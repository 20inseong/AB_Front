package com.example.accountbook_java_edit_ver;

public class fixedItem {
    private String name;
    private String description;
    private int amount;
    private boolean checkOrNot;
    private String date;

    public fixedItem(String name, String description, int amount, boolean checkOrNot, String date) {
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.checkOrNot = checkOrNot;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isCheckOrNot() {
        return checkOrNot;
    }

    public String getDate(){
        return date;
    }

    // Setter 메서드
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCheckOrNot(boolean checkOrNot) {
        this.checkOrNot = checkOrNot;
    }
}

