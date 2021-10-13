package com.akapps.check_vertification_system_v1.classes;

public class VerificationHistory {

    private String dateVerified;
    private String storeName;

    public VerificationHistory(){}

    public VerificationHistory(String dateVerified, String storeName) {
        this.dateVerified = dateVerified;
        this.storeName = storeName;
    }

    public String getDateVerified() {
        return dateVerified;
    }

    public void setDateVerified(String dateVerified) {
        this.dateVerified = dateVerified;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public String toString() {
        return "VerificationHistory{" +
                "dateVerified='" + dateVerified + '\'' +
                ", storeName='" + storeName + '\'' +
                '}';
    }
}
