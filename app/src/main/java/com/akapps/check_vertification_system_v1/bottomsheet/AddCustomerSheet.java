package com.akapps.check_vertification_system_v1.bottomsheet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.akapps.check_vertification_system_v1.R;
import com.akapps.check_vertification_system_v1.activities.MainActivity;
import com.akapps.check_vertification_system_v1.classes.Customer;
import com.akapps.check_vertification_system_v1.classes.FirestoreDatabase;
import com.akapps.check_vertification_system_v1.classes.Helper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.github.drjacky.imagepicker.ImagePicker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import www.sanju.motiontoast.MotionToast;
import static android.app.Activity.RESULT_OK;

public class AddCustomerSheet extends RoundedBottomSheetDialogFragment{

    // taking photos
    private ActivityResultLauncher<Intent> launcher;
    private boolean isProfilePicSelected;
    private Uri profileImageUri;
    private Uri licenseImageUri;

    // variables
    private boolean isAddMode;
    private boolean isEditing;
    private boolean isViewing;
    private Customer customer;

    // layout
    private TextView title;
    private ImageView closeFilter;
    private TextInputLayout nameLayout;
    private TextInputEditText nameInput;
    private TextInputLayout yearLayout;
    private TextInputEditText yearInput;
    private MaterialButton addCustomer;
    private MaterialCardView customerLicenseLayout;
    private ImageView customerLicense;
    private ShapeableImageView customerPhoto;
    private TextView changeProfilePicText;
    private TextView changeIdPicText;
    private ImageView editCustomer;
    private LottieAnimationView nfcTapButton;
    private LottieAnimationView verificationHistoryButton;
    private LinearLayout warningLayout;
    private SwitchMaterial doNotCashSwitch;

    // activity
    private FragmentActivity currentActivity;

    // database
    private FirestoreDatabase firestoreDatabase;

    public AddCustomerSheet(){ }

    public AddCustomerSheet(FragmentActivity currentActivity){
        isAddMode  = true;
        this.currentActivity = currentActivity;
    }

    public AddCustomerSheet(Customer customer, FragmentActivity currentActivity){
        isViewing = true;
        this.customer = customer;
        this.currentActivity = currentActivity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_customer_sheet, container, false);
        view.setBackgroundColor(requireContext().getColor(R.color.grayDark));

        initializeLayout(view);

        if(isAddMode)
            enableAddMode();
        else if(isViewing)
            enableViewMode();

        return view;
    }

    private void openPictureDialog(){
        ImagePicker.Builder camera = ImagePicker.Companion.with(getActivity())
                .crop()
                .cropFreeStyle()
                .maxResultSize(1080, 1080, true);

        if(isProfilePicSelected)
            camera.cropOval();

        camera.createIntentFromDialog((Function1) (new Function1() {
            public Object invoke(Object var1) {
                this.invoke((Intent) var1);
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull Intent it) {
                Intrinsics.checkNotNullParameter(it, "it");
                launcher.launch(it);
            }
        }));
    }

    private void initializeLayout(View view){
        closeFilter = view.findViewById(R.id.close_filter);
        title = view.findViewById(R.id.title);
        nameLayout = view.findViewById(R.id.insert_name_layout);
        nameInput = view.findViewById(R.id.insert_name);
        yearLayout = view.findViewById(R.id.insert_dob_layout);
        yearInput = view.findViewById(R.id.insert_dob);
        addCustomer = view.findViewById(R.id.add_customer);
        customerLicenseLayout = view.findViewById(R.id.customer_license_layout);
        customerLicense = view.findViewById(R.id.customer_license);
        customerPhoto = view.findViewById(R.id.customer_photo);
        changeProfilePicText = view.findViewById(R.id.change_profile_pic_text);
        changeIdPicText = view.findViewById(R.id.change_id_pic_text);
        editCustomer = view.findViewById(R.id.edit_customer);
        nfcTapButton = view.findViewById(R.id.nfc_tap_animation);
        verificationHistoryButton = view.findViewById(R.id.tap_history_animation);
        warningLayout = view.findViewById(R.id.warning_layout);
        doNotCashSwitch = view.findViewById(R.id.do_not_cash_switch);

        firestoreDatabase =  ((MainActivity) currentActivity).firestoreDatabase;

        // catches result of taking a photo or selecting from gallery
        launcher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri uri = result.getData().getData();
                        if(isProfilePicSelected) {
                            profileImageUri = uri;
                            Glide.with(getContext())
                                    .load(uri)
                                    .centerInside()
                                    .placeholder(getActivity().getDrawable(R.drawable.user_icon))
                                    .into(customerPhoto);
                        }
                        else{
                            licenseImageUri = uri;
                            Glide.with(getContext())
                                    .load(uri)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(60)))
                                    .placeholder(getActivity().getDrawable(R.drawable.user_icon))
                                    .into(customerLicense);
                        }
                    } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {}
                });

        closeFilter.setOnClickListener(v -> {
            if(isEditing)
                viewCustomerMode(false);
            else
                this.dismiss();
        });

        editCustomer.setOnClickListener(v -> {
            if(isViewing)
                enableEditMode();
            else if(isEditing){
                Helper.showMessage(getActivity(), getString(R.string.delete_customer_title),
                        getString(R.string.delete_customer_instruction),
                        MotionToast.TOAST_WARNING);
            }
        });

        editCustomer.setOnLongClickListener(v -> {
            firestoreDatabase.deleteCustomer(customer.getCustomerUniqueId());
            this.dismiss();
            return false;
        });

        customerLicenseLayout.setOnClickListener(v -> {
            isProfilePicSelected = false;
            openPictureDialog();
        });

        customerPhoto.setOnClickListener(v -> {
            isProfilePicSelected = true;
            openPictureDialog();
        });

        changeProfilePicText.setOnClickListener(v -> {
            isProfilePicSelected = true;
            openPictureDialog();
        });

        nfcTapButton.setOnClickListener(v -> {
            if(Helper.checkNfcStatus(getActivity(), getContext())) {
                ((MainActivity) getContext()).showNfcPrompt(customer.getCustomerUniqueId(), false);
                this.dismiss();
            }
        });

        verificationHistoryButton.setOnClickListener(v -> {
            if(customer.getVerificationHistory().size() == 0){
                Helper.showMessage(getActivity(), getString(R.string.history_error_title),
                        getString(R.string.history_error_message),
                        MotionToast.TOAST_ERROR);
            }
            else
                ((MainActivity) currentActivity).bottomSheetHelper.openHistorySheet(firestoreDatabase.getSortedHistory(customer));
        });

        addCustomer.setOnClickListener(v -> {
            if(isEditing){
                if(profileImageUri != null || licenseImageUri != null  || customer.isDoNotCash() != doNotCashSwitch.isChecked()){
                    if(profileImageUri != null || licenseImageUri != null)
                        firestoreDatabase.uploadImages(profileImageUri, licenseImageUri, customer.getCustomerUniqueId());
                    if(customer.isDoNotCash() != doNotCashSwitch.isChecked())
                        firestoreDatabase.updateCustomerStatus(customer.getCustomerUniqueId(), "doNotCash", doNotCashSwitch.isChecked());
                    this.dismiss();
                    Helper.showMessage(getActivity(), getString(R.string.customer_updated_title),
                            getString(R.string.customer_updated_message),
                            MotionToast.TOAST_SUCCESS);
                }
                else
                    Helper.showMessage(getActivity(), getString(R.string.editing_message_title), getString(R.string.editing_message), MotionToast.TOAST_ERROR);
            }
            else {
                String name = nameInput.getText().toString();
                String year = yearInput.getText().toString();

                String fullName = Helper.formatName(name, getContext());

                if (fullName.length() > 0) {
                    String firstName = fullName.split(getContext().getString(R.string.split))[0];
                    String lastName = fullName.split(getContext().getString(R.string.split))[1];
                    if (year.length() == 4) {
                        if (licenseImageUri != null) {
                            String customerID = firstName.charAt(0) + lastName + "" + Integer.parseInt(year);
                            if(firestoreDatabase.customerExists(customerID)){
                                Helper.showMessage(getActivity(), getString(R.string.customer_exists_title),
                                        getString(R.string.customer_exists_message),
                                        MotionToast.TOAST_ERROR);
                            }
                            else {
                                firestoreDatabase.addCustomer(firstName, lastName, Integer.parseInt(year), "", "");
                                firestoreDatabase.uploadImages(profileImageUri, licenseImageUri, customerID);
                                ((MainActivity) getContext()).showNfcPrompt(customerID, false);
                                this.dismiss();
                            }
                        } else
                            Helper.showMessage(getActivity(), getString(R.string.license_pic_message_title),
                                    getString(R.string.license_pic_message),
                                    MotionToast.TOAST_ERROR);
                    } else
                        yearLayout.setError(getContext().getString(R.string.year_error));
                } else
                    nameLayout.setError(getContext().getString(R.string.name_error));
            }
        });
    }

    private void enableAddMode(){
        resetModes();
        title.setText(getContext().getString(R.string.add_text));
        nfcTapButton.setVisibility(View.GONE);
        verificationHistoryButton.setVisibility(View.GONE);
    }

    private void enableViewMode(){
        resetModes();
        isViewing = true;
        firestoreDatabase = new FirestoreDatabase(getActivity(), getContext());
        title.setText(getContext().getString(R.string.view_text));
        editCustomer.setVisibility(View.VISIBLE);
        viewCustomerMode(false);
        loadData(customer);

        if(customer.isDoNotCash())
            warningLayout.setVisibility(View.VISIBLE);
    }

    private void enableEditMode(){
        resetModes();
        isEditing = true;
        title.setText(getContext().getString(R.string.edit_text));
        viewCustomerMode(isEditing);
        warningLayout.setVisibility(View.VISIBLE);
        doNotCashSwitch.setVisibility(View.VISIBLE);
        doNotCashSwitch.setChecked(customer.isDoNotCash());
    }

    private void resetModes(){
        isAddMode = false;
        isViewing = false;
        isEditing = false;
    }

    private void viewCustomerMode(boolean status){
        nameInput.setEnabled(false);
        yearInput.setEnabled(false);
        changeProfilePicText.setEnabled(status);
        customerLicenseLayout.setEnabled(status);
        customerPhoto.setEnabled(status);

        if(!status) {
            // status is false (aka viewing)
            resetModes();
            isViewing = true;
            title.setText(getContext().getString(R.string.view_text));
            editCustomer.setImageDrawable(getContext().getDrawable(R.drawable.edit_icon));
            doNotCashSwitch.setVisibility(View.GONE);
            if(!customer.isDoNotCash())
                warningLayout.setVisibility(View.GONE);
        }
        else {
            // status is true (aka editing)
            editCustomer.setImageDrawable(getContext().getDrawable(R.drawable.delete_icon));
            addCustomer.setText(getContext().getString(R.string.edit_text));
            addCustomer.setBackgroundColor(getContext().getColor(R.color.flamingo));
        }

        addCustomer.setVisibility(status ? View.VISIBLE: View.GONE);
        changeIdPicText.setVisibility(status ? View.VISIBLE: View.GONE);
        changeProfilePicText.setVisibility(status ? View.VISIBLE: View.GONE);
        nfcTapButton.setVisibility(status ? View.GONE: View.VISIBLE);
        verificationHistoryButton.setVisibility(status ? View.GONE: View.VISIBLE);
    }

    private void loadData(Customer customer){
        String fullName = customer.getFirstName() + " " + customer.getLastName();
        nameInput.setText(fullName);
        yearInput.setText("" + customer.getDobYear());

        final long ONE_MEGABYTE = 1024 * 1024;
        if(!customer.getProfilePicPath().isEmpty()) {
            StorageReference profilePicRef = FirebaseStorage.getInstance().getReference(customer.getProfilePicPath());
            // gets profile photo from firebase storage
            profilePicRef.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(bytes -> Glide.with(getContext())
                            .load(bytes)
                            .circleCrop()
                            .placeholder(getActivity().getDrawable(R.drawable.user_icon))
                            .into(customerPhoto));
        }

        StorageReference idPicRef = FirebaseStorage.getInstance().getReference(customer.getCustomerIDPath());
        // gets ID photo from firebase storage
        idPicRef.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(bytes -> Glide.with(getContext())
                        .load(bytes)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(60)))
                        .placeholder(getActivity().getDrawable(R.drawable.user_icon))
                        .into(customerLicense));
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