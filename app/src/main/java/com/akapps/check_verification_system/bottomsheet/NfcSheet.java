package com.akapps.check_verification_system.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieAnimationView;
import com.akapps.check_verification_system.R;
import com.bumptech.glide.Glide;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

public class NfcSheet extends RoundedBottomSheetDialogFragment{

    private StorageReference profileStoragePath;
    private String customerName;

    public NfcSheet(){}

    public NfcSheet(StorageReference profileStoragePath, String customerName){
        this.profileStoragePath = profileStoragePath;
        this.customerName = customerName;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_nfc_tap, container, false);
        view.setBackgroundColor(requireContext().getColor(R.color.grayDark));

        ImageView closeFilter = view.findViewById(R.id.close_filter);
        ImageView profileImage = view.findViewById(R.id.profile_image);
        TextView customerNameText = view.findViewById(R.id.customer_name);

        closeFilter.setOnClickListener(v -> this.dismiss());

        populateCustomer(profileImage, customerNameText);

        return view;
    }

    private void populateCustomer(ImageView profileImage, TextView customerNameText){
        if(profileStoragePath != null) {
            Glide.with(getContext())
                    .load(profileStoragePath)
                    .circleCrop()
                    .placeholder(getActivity().getDrawable(R.drawable.user_icon))
                    .into(profileImage);
        }

        if(customerName !=null && !customerName.isEmpty())
            customerNameText.setText(customerName);
        else
            customerNameText.setVisibility(View.INVISIBLE);
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