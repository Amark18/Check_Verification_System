package com.akapps.check_verification_system.bottomsheet;

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
import com.akapps.check_verification_system.R;
import com.akapps.check_verification_system.activities.MainActivity;
import com.akapps.check_verification_system.classes.Customer;
import com.akapps.check_verification_system.classes.FirestoreDatabase;
import com.akapps.check_verification_system.classes.Helper;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.imageviewer.StfalconImageViewer;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import www.sanju.motiontoast.MotionToast;
import static android.app.Activity.RESULT_OK;

public class AddCustomerSheet extends RoundedBottomSheetDialogFragment {

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
    private boolean isUpdatingCustomer;

    // layout
    private TextView title;
    private ImageView closeFilter;
    private TextInputLayout nameLayout;
    private TextInputEditText nameInput;
    private TextInputLayout yearLayout;
    private TextInputEditText yearInput;
    private TextInputLayout phoneNumberLayout;
    private TextInputEditText phoneNumberInput;
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
    private TextView cardReadText;
    private TextView storeAccount;

    // activity
    private FragmentActivity currentActivity;

    // database
    private FirestoreDatabase firestoreDatabase;
    private FirebaseStorage firebaseStorage;

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
        View view = inflater.inflate(R.layout.bottom_sheet_add_customer, container, false);
        view.setBackgroundColor(requireContext().getColor(R.color.grayDark));

        initializeLayout(view);

        if(isAddMode)
            enableAddMode();
        else if(isViewing) {
            // show local customer right way before attempting to update
            enableViewMode();
            // makes sure that this customer actually exists since local copy is being using at the moment
            getLiveCustomerData();
        }

        return view;
    }

    private void getLiveCustomerData(){
        firestoreDatabase.getCustomersCollectionRef().document(customer.getCustomerUniqueId())
                .get(Source.SERVER).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            firestoreDatabase.checkConnection(true);
                            // live means up-to-date
                            Customer liveCustomer = document.toObject(Customer.class);
                            // prevents writing to database if customer was viewed today
                            firestoreDatabase.updateCustomer(liveCustomer.getCustomerUniqueId(), "dateViewed", Helper.getDatabaseDate());
                            if(firestoreDatabase.updateLocalCustomer(customer, liveCustomer)) {
                                // update customer view status
                                // local customer data needs to be updated since there is new data
                                customer = liveCustomer;
                                enableViewMode();
                            }
                        }
                        else {
                            // customer does not exist in database so it was deleted by another user
                            deleteCustomer();
                        }
                    }
                    else {
                        // error (cannot get data from database...probably from no internet connection)
                        // use local data instead
                        firestoreDatabase.checkConnection(false);
                        enableViewMode();
                    }
        });
    }

    private void openPictureDialog(){
        ImagePicker.Builder camera = ImagePicker.Companion.with(getActivity())
                .crop()
                .cropFreeStyle()
                .maxResultSize(384, 384, true);

        if(isProfilePicSelected)
            camera.cropOval();

        // required function to use kotlin library
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
        phoneNumberLayout = view.findViewById(R.id.insert_phone_number_layout);
        phoneNumberInput= view.findViewById(R.id.insert_phone_number);
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
        cardReadText = view.findViewById(R.id.account_text);
        storeAccount = view.findViewById(R.id.account_store);

        // if orientation changes, this prevents app from crashing by closing this bottom sheet
        try {
            firestoreDatabase =  ((MainActivity) currentActivity).firestoreDatabase;
        }
        catch (Exception e){
            this.dismiss();
        }

        // catches result of taking a photo or selecting from gallery
        launcher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri uri = result.getData().getData();
                        if(isProfilePicSelected) {
                            profileImageUri = uri;
                            Glide.with(getContext())
                                    .load(uri)
                                    .circleCrop()
                                    .placeholder(getActivity().getDrawable(R.drawable.user_icon))
                                    .into(customerPhoto);
                        }
                        else{
                            licenseImageUri = uri;
                            Glide.with(getContext())
                                    .load(uri)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(60)))
                                    .placeholder(getActivity().getDrawable(R.drawable.id_card_icon))
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
            deleteCustomer();
            return false;
        });

        customerLicenseLayout.setOnClickListener(v -> {
            if(isViewing)
                openImagesFullscreen(customerLicense, 1);
            else if(isAddMode || isEditing) {
                isProfilePicSelected = false;
                openPictureDialog();
            }
        });

        customerPhoto.setOnClickListener(v -> {
            if(isViewing)
                openImagesFullscreen(customerPhoto, 0);
            else if(isAddMode || isEditing) {
                isProfilePicSelected = true;
                openPictureDialog();
            }
        });

        changeProfilePicText.setOnClickListener(v -> {
            if(isAddMode || isEditing) {
                isProfilePicSelected = true;
                openPictureDialog();
            }
        });

        nfcTapButton.setOnClickListener(v -> {
            if(Helper.checkNfcStatus(getActivity(), getContext())) {
                showNfcPrompt(customer.getCustomerUniqueId());
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
                String newPhoneNumber = phoneNumberInput.getText().toString();
                boolean isNewPhoneNumber = !newPhoneNumber.equals(customer.getPhoneNumber())
                        && newPhoneNumber.length() == 10;
                boolean isDeletingPhoneNumber = !newPhoneNumber.equals(customer.getPhoneNumber())
                        && newPhoneNumber.length() == 0;
                // checks to see if profile image / check image / customer check status / or phone number was changed
                if(profileImageUri != null || licenseImageUri != null  ||
                        customer.isDoNotCash() != doNotCashSwitch.isChecked() ||
                        isNewPhoneNumber || isDeletingPhoneNumber){
                    if(profileImageUri != null || licenseImageUri != null)
                        firestoreDatabase.uploadImages(profileImageUri, licenseImageUri, customer);
                    if(customer.isDoNotCash() != doNotCashSwitch.isChecked()) {
                        firestoreDatabase.updateCustomerStatus(customer.getCustomerUniqueId(), "doNotCash", doNotCashSwitch.isChecked());
                        // update local copy to view changes
                        customer.setDoNotCash(doNotCashSwitch.isChecked());
                    }
                    if(isNewPhoneNumber || isDeletingPhoneNumber) {
                        firestoreDatabase.updateCustomer(customer.getCustomerUniqueId(), "phoneNumber", newPhoneNumber);
                        // update local copy to view changes
                        customer.setPhoneNumber(newPhoneNumber);
                    }
                    firestoreDatabase.loadCustomerData(true);
                    isUpdatingCustomer = true;
                    enableViewMode();
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
                String phoneNumber = phoneNumberInput.getText().toString();

                String fullName = Helper.formatName(name, getContext());

                // if input is correct, then customer is added to the system
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
                                firestoreDatabase.addCustomer(firstName, lastName, Integer.parseInt(year),
                                        phoneNumber.length() == 10 ? phoneNumber : "", "", "");
                                firestoreDatabase.uploadImages(profileImageUri, licenseImageUri, new Customer(customerID));

                                // based on user setting, nfc card prompts will be shown
                                String showNfcPromptStatus = Helper.getPreference(getContext(),
                                        getContext().getString(R.string.show_nfc_prompt_pref));
                                if(showNfcPromptStatus != null && !showNfcPromptStatus.equals(""))
                                   showNfcPrompt(customerID);

                                // update dashboard
                                firestoreDatabase.loadCustomerData(true);
                                Helper.showMessage(getActivity(), getString(R.string.customer_added_title),
                                        getString(R.string.customer_added_message),
                                        MotionToast.TOAST_SUCCESS);
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

    private void showNfcPrompt(String customerID){
        ((MainActivity) getContext()).showNfcPrompt(customerID, false,
                customer.getProfilePicPath() == null || customer.getProfilePicPath().equals("") ?
                        null : firebaseStorage.getReference(customer.getProfilePicPath()),
                customer.getFirstName() + " " + customer.getLastName());
    }

    private void enableAddMode(){
        resetModes();
        isAddMode = true;
        title.setText(getContext().getString(R.string.add_text));
        nfcTapButton.setVisibility(View.GONE);
        verificationHistoryButton.setVisibility(View.GONE);
        warningLayout.setVisibility(View.GONE);
        cardReadText.setVisibility(View.GONE);
        storeAccount.setVisibility(View.GONE);
    }

    private void enableViewMode(){
        try {
            resetModes();
            isViewing = true;
            title.setText(getContext().getString(R.string.view_text));
            storeAccount.setText(customer.getStoreAdded());
            editCustomer.setVisibility(View.VISIBLE);
            viewCustomerMode(false);
            loadData();

            if(customer.isDoNotCash())
                warningLayout.setVisibility(View.VISIBLE);
        }catch (Exception e){ }
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

        if(!status) {
            // status is false (aka viewing)
            resetModes();
            isViewing = true;
            title.setText(getContext().getString(R.string.view_text));
            editCustomer.setImageDrawable(getContext().getDrawable(R.drawable.edit_icon));
            doNotCashSwitch.setVisibility(View.GONE);
            if(!customer.isDoNotCash())
                warningLayout.setVisibility(View.GONE);
            phoneNumberInput.setEnabled(false);
            phoneNumberInput.setText(customer.getPhoneNumber() == null || customer.getPhoneNumber().equals("") ? "N/A" : customer.getPhoneNumber());
            if(customer.getPhoneNumber() != null && customer.getPhoneNumber().length() == 10)
                phoneNumberLayout.setHint(R.string.phone_number_filled_input_hint);
        }
        else {
            // status is true (aka editing)
            editCustomer.setImageDrawable(getContext().getDrawable(R.drawable.delete_icon));
            addCustomer.setText(getContext().getString(R.string.edit_text));
            addCustomer.setBackgroundColor(getContext().getColor(R.color.flamingo));
            phoneNumberInput.setEnabled(true);
            // if there is no phone number set, it changes it from N/A to empty so it can be edited faster
            if(null == customer.getPhoneNumber() || customer.getPhoneNumber().equals(""))
                phoneNumberInput.setText("");
        }

        addCustomer.setVisibility(status ? View.VISIBLE: View.GONE);
        changeIdPicText.setVisibility(status ? View.VISIBLE: View.GONE);
        changeProfilePicText.setVisibility(status ? View.VISIBLE: View.GONE);
        nfcTapButton.setVisibility(status ? View.GONE: View.VISIBLE);
        verificationHistoryButton.setVisibility(status ? View.GONE: View.VISIBLE);
        cardReadText.setVisibility(status ? View.GONE: View.VISIBLE);
        storeAccount.setVisibility(status ? View.GONE: View.VISIBLE);
    }

    // loads profile picture and ID picture from firebase storage
    private void loadData(){
        firebaseStorage = FirebaseStorage.getInstance();
        String fullName = customer.getFirstName() + " " + customer.getLastName();
        nameInput.setText(fullName);
        yearInput.setText("" + customer.getDobYear());
        phoneNumberInput.setText(customer.getPhoneNumber() == null || customer.getPhoneNumber().equals("") ? "N/A" : "" + customer.getPhoneNumber());
        // gets profile and ID pictures from database and populates it for user to see
        if(!isUpdatingCustomer)
            populateImages();
        else
            isUpdatingCustomer = false;
    }

    private void populateImages(){
        if(!customer.getProfilePicPath().isEmpty()) {
            // gets profile photo from firebase storage
            Glide.with(getContext())
                    .load(firebaseStorage.getReference(customer.getProfilePicPath()))
                    .circleCrop()
                    .placeholder(getActivity().getDrawable(R.drawable.user_icon))
                    .into(customerPhoto);
        }

        if(!customer.getCustomerIDPath().isEmpty()) {
            // gets ID photo from firebase storage
            Glide.with(getContext())
                    .load(firebaseStorage.getReference(customer.getCustomerIDPath()))
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(60)))
                    .placeholder(getActivity().getDrawable(R.drawable.id_card_icon))
                    .into(customerLicense);
        }
    }

    private void openImagesFullscreen(ImageView view, int start){
        ArrayList<String> images = new ArrayList<>();
        if(!customer.getProfilePicPath().isEmpty())
            images.add(customer.getProfilePicPath());
        if(!customer.getCustomerIDPath().isEmpty())
            images.add(customer.getCustomerIDPath());

        // if start position is same size as list (aka out of bounds), decrement it.
        // occurs when there is only an ID picture (profile pic was not set)
        if(images.size() == start)
            start--;

        new StfalconImageViewer.Builder<>(getContext(), images, (imageView, image) ->
                Glide.with(getContext())
                        .load(FirebaseStorage.getInstance().getReference(image))
                        .into(imageView))
                .withBackgroundColor(getContext().getColor(R.color.grayDark))
                .allowZooming(true)
                .allowSwipeToDismiss(true)
                .withHiddenStatusBar(false)
                .withStartPosition(start)
                .withTransitionFrom(view)
                .show();
    }

    private void deleteCustomer(){
        firestoreDatabase.deleteCustomer(customer);
        Helper.showMessage(getActivity(), getString(R.string.customer_deleted_title),
                getString(R.string.customer_deleted_message),
                MotionToast.TOAST_WARNING);
        this.dismiss();
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