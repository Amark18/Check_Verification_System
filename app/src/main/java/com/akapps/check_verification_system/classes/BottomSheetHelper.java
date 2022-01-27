package com.akapps.check_verification_system.classes;

import androidx.fragment.app.FragmentActivity;
import com.akapps.check_verification_system.bottomsheet.AddCustomerSheet;
import com.akapps.check_verification_system.bottomsheet.InfoSheet;
import com.akapps.check_verification_system.bottomsheet.NfcSheet;
import com.akapps.check_verification_system.bottomsheet.SettingsSheet;
import com.akapps.check_verification_system.bottomsheet.VerificationHistorySheet;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * This class ensures that there is only one bottom sheet
 * open at a time and prevents duplicates
 */

public class BottomSheetHelper {

    // activity
    private FragmentActivity currentActivity;

    // bottom sheets
    private AddCustomerSheet addCustomerSheet;
    private NfcSheet nfcSheet;
    private VerificationHistorySheet historySheet;
    private InfoSheet infoSheet;
    private SettingsSheet settingsSheet;

    public BottomSheetHelper(){}

    public BottomSheetHelper(FragmentActivity currentActivity) {
        this.currentActivity = currentActivity;
    }

    public void openCustomerSheet(FragmentActivity currentActivity, Customer customer){
        if(addCustomerSheet != null)
            addCustomerSheet.dismiss();
        if(currentActivity!=null && customer == null)
            addCustomerSheet = new AddCustomerSheet(currentActivity);
        else if(customer != null)
            addCustomerSheet = new AddCustomerSheet(customer, currentActivity);
        if(currentActivity != null || customer!= null)
            addCustomerSheet.show(currentActivity.getSupportFragmentManager(), addCustomerSheet.getTag());
    }

    public void openNfcSheet(String titleMessage){
        if(nfcSheet != null)
            nfcSheet.dismiss();
        nfcSheet = new NfcSheet(titleMessage);
        nfcSheet.show(currentActivity.getSupportFragmentManager(), nfcSheet.getTag());
    }

    public void openNfcSheet(StorageReference profileStoragePath, String customerName){
        if(nfcSheet != null)
            nfcSheet.dismiss();
        nfcSheet = new NfcSheet(profileStoragePath, customerName);
        nfcSheet.show(currentActivity.getSupportFragmentManager(), nfcSheet.getTag());
    }

    public void closeNfcSheet(){
        if(nfcSheet != null)
            nfcSheet.dismiss();
    }

    public void openHistorySheet(ArrayList<VerificationHistory> history){
        if(historySheet != null)
            historySheet.dismiss();
        historySheet = new VerificationHistorySheet(history);
        historySheet.show(currentActivity.getSupportFragmentManager(), historySheet.getTag());
    }

    public void openInfoSheet(){
        if(infoSheet != null)
            infoSheet.dismiss();
        infoSheet = new InfoSheet();
        infoSheet.show(currentActivity.getSupportFragmentManager(), infoSheet.getTag());
    }

    public void openSettingsSheet(String nfcData){
        if(settingsSheet != null)
            settingsSheet.dismiss();
        if(nfcData.isEmpty())
            settingsSheet = new SettingsSheet(currentActivity);
        else
            settingsSheet = new SettingsSheet(nfcData, currentActivity);
        settingsSheet.show(currentActivity.getSupportFragmentManager(), settingsSheet.getTag());
    }

    public void closeAllSheets(){
        // if orientation changes, this prevents app from crashing
        try {
            if (addCustomerSheet != null)
                addCustomerSheet.dismiss();
            if (nfcSheet != null)
                nfcSheet.dismiss();
            if (historySheet != null)
                historySheet.dismiss();
            if (settingsSheet != null)
                settingsSheet.dismiss();
            if (infoSheet != null)
                infoSheet.dismiss();
        }
        catch (Exception e){}
    }
}
