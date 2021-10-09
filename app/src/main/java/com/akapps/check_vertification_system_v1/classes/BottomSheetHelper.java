package com.akapps.check_vertification_system_v1.classes;

import androidx.fragment.app.FragmentActivity;
import com.akapps.check_vertification_system_v1.bottomsheet.AddCustomerSheet;
import com.akapps.check_vertification_system_v1.bottomsheet.NfcSheet;
import com.akapps.check_vertification_system_v1.bottomsheet.SettingsSheet;
import com.akapps.check_vertification_system_v1.bottomsheet.VerificationHistorySheet;
import java.util.ArrayList;

public class BottomSheetHelper {

    // activity
    private FragmentActivity currentActivity;

    // bottom sheets
    private AddCustomerSheet addCustomerSheet;
    private NfcSheet nfcSheet;
    private VerificationHistorySheet historySheet;
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
            addCustomerSheet = new AddCustomerSheet(customer, currentActivity, -1);
        if(currentActivity != null || customer!= null)
            addCustomerSheet.show(currentActivity.getSupportFragmentManager(), addCustomerSheet.getTag());
    }

    public void openNfcSheet(){
        if(nfcSheet != null)
            nfcSheet.dismiss();
        nfcSheet = new NfcSheet();
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

    public void openSettingsSheet(String nfcData){
        if(settingsSheet != null)
            settingsSheet.dismiss();
        if(nfcData.isEmpty())
            settingsSheet = new SettingsSheet();
        else
            settingsSheet = new SettingsSheet(nfcData);
        settingsSheet.show(currentActivity.getSupportFragmentManager(), settingsSheet.getTag());
    }

    public void closeAllSheets(){
        if(addCustomerSheet != null)
            addCustomerSheet.dismiss();
        if(nfcSheet != null)
            nfcSheet.dismiss();
        if(historySheet != null)
            historySheet.dismiss();
        if(settingsSheet != null)
            settingsSheet.dismiss();
    }
}
