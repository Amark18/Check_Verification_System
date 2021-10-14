package com.akapps.check_vertification_system_v1.bottomsheet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.check_vertification_system_v1.R;
import com.akapps.check_vertification_system_v1.classes.Helper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import www.sanju.motiontoast.MotionToast;

public class AccountLoginSheet extends RoundedBottomSheetDialogFragment{

    // account authentication
    private FirebaseAuth mAuth;

    // variables
    private int loginAttempts;
    private final int maxLoginAttempts = 5;
    private BottomSheetDialog dialog;

    // layout
    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private FloatingActionButton loginButton;

    public AccountLoginSheet(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_account_login, container, false);
        view.setBackgroundColor(requireContext().getColor(R.color.grayDark));

        // initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // layout
        emailLayout = view.findViewById(R.id.insert_email_layout);
        emailInput = view.findViewById(R.id.insert_email);
        passwordLayout = view.findViewById(R.id.insert_password_layout);
        passwordInput = view.findViewById(R.id.insert_password);
        loginButton = view.findViewById(R.id.login);

        loginButton.setOnClickListener(view1 -> getInput());

        return view;
    }

    private void login(String email, String password){
        if(loginAttempts <= maxLoginAttempts) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), (OnCompleteListener<AuthResult>) task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Helper.savePreference(getContext(), "true",
                                    getContext().getString(R.string.account_login_pref));
                            Helper.showMessage(getActivity(), getContext().getString(R.string.login_success_title),
                                    getContext().getString(R.string.login_success_message),
                                    MotionToast.TOAST_SUCCESS);
                            dialog.dismiss();
                        } else {
                            loginAttempts++;
                            // If sign in fails, display a message to the user.
                            Helper.showMessage(getActivity(), getContext().getString(R.string.login_error_title),
                                    getContext().getString(R.string.login_error_message),
                                    MotionToast.TOAST_ERROR);
                        }
                    });
        }
        else
            Helper.showMessage(getActivity(), getContext().getString(R.string.login_max_title),
                    getContext().getString(R.string.login_max_message),
                    MotionToast.TOAST_ERROR);
    }

    private void getInput(){
        String inputEmail = emailInput.getText().toString();
        String inputPassword = passwordInput.getText().toString();
        if(!inputEmail.isEmpty() && inputEmail.contains(getContext().getString(R.string.email_end_format))){
            emailLayout.setErrorEnabled(false);
            if(!inputPassword.isEmpty())
                login(inputEmail, inputPassword);
            else
                passwordLayout.setError(getContext().getString(R.string.input_error));
        }
        else
            emailLayout.setError(getContext().getString(R.string.input_error));
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
                    dialog =(BottomSheetDialog) getDialog ();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
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