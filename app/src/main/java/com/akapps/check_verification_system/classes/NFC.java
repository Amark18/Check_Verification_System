package com.akapps.check_verification_system.classes;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.akapps.check_verification_system.R;
import com.akapps.check_verification_system.activities.MainActivity;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import www.sanju.motiontoast.MotionToast;

public class NFC {

    // NFC
    private final NfcAdapter nfcAdapter;
    private final PendingIntent pendingIntent;

    // activity
    private final FragmentActivity currentActivity;
    private final Context context;

    // layout
    private ImageView nfcStatus;

    // variables
    private final int shortDuration = 800;
    private BottomSheetHelper bottomSheetHelper;

    public NFC(FragmentActivity currentActivity, Context context, NfcAdapter nfcAdapter, PendingIntent pendingIntent,
               ImageView nfcStatus, BottomSheetHelper bottomSheetHelper){
        this.currentActivity = currentActivity;
        this.context = context;
        this.nfcAdapter = nfcAdapter;
        this.pendingIntent = pendingIntent;
        this.nfcStatus = nfcStatus;
        this.bottomSheetHelper = bottomSheetHelper;
        checkNfcStatus();
    }

    // checks current status of nfc (enabled or not by the user) and updates NFC icon to reflect that
    public boolean checkNfcStatus(){
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            nfcStatus.setColorFilter(ContextCompat.getColor(context,
                    R.color.green));
            return true;
        }
        else if(adapter != null && !adapter.isEnabled()){
            nfcStatus.setColorFilter(ContextCompat.getColor(context,
                    R.color.vermilion));
        }
        return false;
    }

    // if nfc is not turned on, then it will take them to settings where they can change it
    public void isNfcDisabled(){
        Helper.showMessage(currentActivity, context.getString(R.string.nfc_text),
                context.getString(R.string.nfc_message),
                MotionToast.TOAST_WARNING);
        new Handler().postDelayed(() -> {
            currentActivity.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }, shortDuration);
    }

    // opens nfc bottom sheet to prompt user to tap card
    public void showNfcPrompt(){
        ((MainActivity) currentActivity).bottomSheetHelper.openNfcSheet();
    }

    public void showNfcPrompt(StorageReference profileStoragePath, String customerName){
        ((MainActivity) currentActivity).bottomSheetHelper.openNfcSheet(profileStoragePath, customerName);
    }

    // writes a message to nfc card
    public void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
        Helper.showMessage(currentActivity, context.getString(R.string.write_nfc_title),
                context.getString(R.string.write_nfc_message),
                MotionToast.TOAST_SUCCESS);
    }

    // creates record to put in nfc card
    public NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        payload[0] = (byte) langLength;

        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);
    }

    // read nfc card
    public void readNFCTag(Intent intent, boolean write, String writeText) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        String[] techList = tag.getTechList();
        for (int i = 0; i < techList.length; i++) {
            if (techList[i].equals(Ndef.class.getName())) {
                Ndef ndef = Ndef.get(tag);
                try {
                    ndef.connect();
                    NdefMessage ndefMessage = ndef.getNdefMessage();

                    if(ndefMessage != null) {
                        NdefRecord[] e = ndefMessage.getRecords();

                        for (NdefRecord s : e) {
                            String message = new String(s.getPayload());
                            if (!message.equals("") || write) {
                                if(write) {
                                    ndef.close();
                                    write(writeText, tag);
                                }
                                else if(writeText.equals(context.getString(R.string.output_data)))
                                    bottomSheetHelper.openSettingsSheet(message);
                                else
                                    ((MainActivity) context).findCustomer(filterNfcId(message));
                            }
                            else
                                Helper.showMessage(currentActivity, "Read Successful", "NFC Card Empty",
                                        MotionToast.TOAST_ERROR);
                        }
                    }
                    else
                        Helper.showMessage(currentActivity, "Read Failed", "Try again!",
                                MotionToast.TOAST_ERROR);
                    ndef.close();
                }
                catch (FormatException | IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private String filterNfcId(String id){
        return id.substring(id.indexOf("en") + 2);
    }
}
