package com.akapps.check_vertification_system_v1.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Customer {

    private String firstName;
    private String lastName;
    private int dobYear;
    private String customerUniqueId;
    private String dateAdded;
    private String timeStampAdded;
    private String storeAdded;
    private String dateVerified;
    private String profilePicPath;
    private String customerIDPath;
    private List<VerificationHistory> verificationHistory;
    private boolean doNotCash;

    public Customer(){}

    public Customer(String customerUniqueId){
        this.customerUniqueId = customerUniqueId;
        profilePicPath = customerIDPath = "";
    }

    public Customer(String firstName, String lastName, int dobYear,
                    String customerUniqueId, String dateAdded, String dateVerified,
                    String profilePicPath, String customerIDPath, String timeStampAdded, String storeAdded) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dobYear = dobYear;
        this.customerUniqueId = customerUniqueId;
        this.dateAdded = dateAdded;
        this.dateVerified = dateVerified;
        this.profilePicPath = profilePicPath;
        this.customerIDPath = customerIDPath;
        this.verificationHistory = new ArrayList<>();
        this.timeStampAdded = timeStampAdded;
        this.storeAdded = storeAdded;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getDobYear() {
        return dobYear;
    }

    public void setDobYear(int dobYear) {
        this.dobYear = dobYear;
    }

    public String getCustomerUniqueId() {
        return customerUniqueId;
    }

    public void setCustomerUniqueId(String customerUniqueId) {
        this.customerUniqueId = customerUniqueId;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getDateVerified() {
        return dateVerified;
    }

    public void setDateVerified(String dateVerified) {
        this.dateVerified = dateVerified;
    }

    public String getProfilePicPath() {
        return profilePicPath;
    }

    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }

    public String getCustomerIDPath() {
        return customerIDPath;
    }

    public void setCustomerIDPath(String customerIDPath) {
        this.customerIDPath = customerIDPath;
    }

    public List<VerificationHistory> getVerificationHistory() {
        return verificationHistory;
    }

    public void setVerificationHistory(List<VerificationHistory> verificationHistory) {
        this.verificationHistory = verificationHistory;
    }

    public boolean isDoNotCash() {
        return doNotCash;
    }

    public void setDoNotCash(boolean doNotCash) {
        this.doNotCash = doNotCash;
    }

    public String getTimeStampAdded() {
        return timeStampAdded;
    }

    public void setTimeStampAdded(String timeStampAdded) {
        this.timeStampAdded = timeStampAdded;
    }

    public String getStoreAdded() {
        return storeAdded;
    }

    public void setStoreAdded(String storeAdded) {
        this.storeAdded = storeAdded;
    }
}

