package com.example.accountbook_java_edit_ver;

public class ReceiptResponse {
    private String receiptId;
    private String storeName;
    private double totalAmount;
    private String date;

    // Getters and Setters
    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
