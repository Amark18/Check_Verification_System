package com.akapps.check_verification_system.bottomsheet;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.akapps.check_verification_system.R;
import com.akapps.check_verification_system.activities.MainActivity;
import com.akapps.check_verification_system.classes.Helper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;
import www.sanju.motiontoast.MotionToast;

public class SettingsSheet extends RoundedBottomSheetDialogFragment{

    // variables
    private final int messageDuration = 1500;
    private String dataRead;
    private FragmentActivity currentActivity;

    public SettingsSheet(){}

    public SettingsSheet(FragmentActivity currentActivity){
        this.currentActivity = currentActivity;
    }

    public SettingsSheet(String dataRead, FragmentActivity currentActivity){
        this.dataRead = dataRead;
        this.currentActivity = currentActivity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_settings, container, false);
        view.setBackgroundColor(requireContext().getColor(R.color.grayDark));

        // if orientation changes, this prevents app from crashing by closing this bottom sheet
        if(currentActivity == null)
            this.dismiss();

        ImageView closeFilter = view.findViewById(R.id.close_filter);
        ImageView info = view.findViewById(R.id.info);
        MaterialCardView resetNfcCard = view.findViewById(R.id.reset_nfc_card);
        MaterialCardView readNfcCard = view.findViewById(R.id.read_nfc_card);
        MaterialCardView showNfcPrompt = view.findViewById(R.id.show_nfc_prompt);
        MaterialCardView resetApp = view.findViewById(R.id.reset_app);
        MaterialCardView logOut = view.findViewById(R.id.log_out);
        TextView cardReadText = view.findViewById(R.id.text_read);
        TextView storeAccount = view.findViewById(R.id.account_store);
        SwitchMaterial showNfcSwitch = view.findViewById(R.id.show_nfc_prompt_switch);

        storeAccount.setText(Helper.getStoreName(getContext()));

        if (NfcAdapter.getDefaultAdapter(getContext()) == null) {
            resetNfcCard.setVisibility(View.GONE);
            readNfcCard.setVisibility(View.GONE);
            showNfcPrompt.setVisibility(View.GONE);
        }

        // gets user setting for switch
        String showNfcPromptStatus = Helper.getPreference(getContext(),
                getContext().getString(R.string.show_nfc_prompt_pref));
        showNfcSwitch.setChecked(showNfcPromptStatus == null || showNfcPromptStatus.equals("") ? false: true);

        if(dataRead != null && !dataRead.isEmpty()) {
            dataRead = dataRead.replace("en", "");
            // if card is empty, output that to user
            // if data length is only one character or less, it means it is empty or has a weird symbol
            if(dataRead.length() <= 1)
                dataRead = "CARD EMPTY";
            cardReadText.setText(cardReadText.getText() + "\nCard data: " + dataRead);
        }

        resetNfcCard.setOnClickListener(view12 -> {
            if(Helper.checkNfcStatus(getActivity(), getContext())) {
                ((MainActivity) getContext()).showNfcPrompt("", false);
            }
        });

        readNfcCard.setOnClickListener(view13 -> {
            if(Helper.checkNfcStatus(getActivity(), getContext())) {
                ((MainActivity) getContext()).showNfcPrompt("", true);
            }
        });

        showNfcSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            String currentNfcPromptStatus = b ? "on" : "";
            Helper.savePreference(getContext(), currentNfcPromptStatus,
                    getContext().getString(R.string.show_nfc_prompt_pref));
        });

        resetApp.setOnClickListener(v -> Helper.showMessage(getActivity(),
                getContext().getString(R.string.reset_app),
                getContext().getString(R.string.reset_app_message_1),
                MotionToast.TOAST_WARNING));

        resetApp.setOnLongClickListener(view15 -> {
            Helper.showMessage(getActivity(), getContext().getString(R.string.reset_app),
                    getContext().getString(R.string.reset_app_message_2),
                    MotionToast.TOAST_WARNING);
            new Handler().postDelayed(() -> {
                Helper.clearAppData(getActivity());
            }, messageDuration);
            return false;
        });

        logOut.setOnClickListener(view1 -> logOut());

        info.setOnClickListener(view14 -> ((MainActivity)currentActivity).bottomSheetHelper.openInfoSheet());

        closeFilter.setOnClickListener(v -> this.dismiss());

        return view;
    }

    private void logOut(){
        Helper.savePreference(getContext(), null,
                getContext().getString(R.string.account_login_pref));
        // sign out user
        FirebaseAuth.getInstance().signOut();
        this.dismiss();
        ((MainActivity) currentActivity).logOut();
    }

    @Override
    public int getTheme() {
        return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog =(BottomSheetDialog) getDialog ();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById (R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from (bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}