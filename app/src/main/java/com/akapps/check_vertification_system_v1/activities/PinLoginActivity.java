package com.akapps.check_vertification_system_v1.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.akapps.check_vertification_system_v1.R;
import com.akapps.check_vertification_system_v1.bottomsheet.AccountLoginSheet;
import com.akapps.check_vertification_system_v1.classes.Helper;
import com.google.firebase.auth.FirebaseAuth;
import com.mukesh.OtpView;
import java.util.Calendar;
import java.util.concurrent.Executor;
import www.sanju.motiontoast.MotionToast;

public class PinLoginActivity extends AppCompatActivity{

    // activity
    private Context context;

    // layout
    private OtpView pinInput;
    private LottieAnimationView fingerprintIcon;
    private ImageView lockIcon;
    private TextView loginMessage;

    // variables
    private final int loginErrorDuration = 1500;
    private String lastLoginDate;
    private boolean isLoginNeeded;
    private final int minsInDay = 1440;

    // biometric data
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    // bottom sheet
    private AccountLoginSheet accountLoginSheet;

    @Override
    public void onBackPressed() {
        // catches if back button is pressed
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeLayout();

        // checks to see if user has logged in and if so, then lets them enter a pin afterwards
        if(Helper.isAccountLoggedIn(context))
            // pin login only required once every 24 hours
            autoLogin();
        else
            openAccountLoginSheet();

    }

    private void initializeLayout(){
        context = this;
        // initialize layout
        pinInput = findViewById(R.id.pin_input);
        fingerprintIcon = findViewById(R.id.fingerprint_icon);
        loginMessage = findViewById(R.id.login_message);
        lockIcon = findViewById(R.id.lock_icon);

        // initializing fingerprint
        executor = ContextCompat.getMainExecutor(this);
        if(!isFingerprintWorking() || Helper.getPreference(context, getString(R.string.fingerprint_pref)) == null)
            fingerprintIcon.setVisibility(View.INVISIBLE);
        else
            loginMessage.setText(getString(R.string.login_message_both));

        // sets the size of fingerprint based on device size
        int screenWidth = Helper.getWidthScreen(this);
        fingerprintIcon.getLayoutParams().width = screenWidth / 2;
        fingerprintIcon.getLayoutParams().height = screenWidth / 2;

        // setting up pin variables
        pinInput.setAnimationEnable(true);
        pinInput.setMaskingChar("*");
        pinInput.setItemCount(getCurrentPin().length());

        pinInput.setOtpCompletionListener(userInput -> {
            if(userInput.equals(getCurrentPin()))
                openMainPage();
            else{
                // user entered wrong input
                lockIcon.setColorFilter(getColor(R.color.vermilion));
                pinInput.setText("");
                loginMessage.setText(getString(R.string.try_again));

                new Handler().postDelayed(() -> {
                    // resets lock icon color and message after 1.5 seconds
                    lockIcon.setColorFilter(getColor(R.color.blueDark));
                    loginMessage.setText(getString(R.string.login_message_pin_only));
                }, loginErrorDuration);
            }
        });

        fingerprintIcon.setOnClickListener(v -> getFingerprintFromUser());

        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                openMainPage();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Helper.showMessage(PinLoginActivity.this, getString(R.string.fingerprint_error_text), getString(R.string.fingerprint_error),
                        MotionToast.TOAST_ERROR);
            }
        });
    }

    private void openAccountLoginSheet(){
        accountLoginSheet = new AccountLoginSheet();
        accountLoginSheet.show(getSupportFragmentManager(), accountLoginSheet.getTag());
    }

    // pin is based on the current year and the hour
    private String getCurrentPin(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.HOUR);
    }

    private void getFingerprintFromUser(){
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.fingerprint))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    // checks to see if phone has fingerprint capability
    private boolean isFingerprintWorking(){
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            default:
                return false;
        }
    }

    // if the time since last login is less than 24 hours, log user in automatically
    private void autoLogin(){
        lastLoginDate = Helper.getPreference(context, getString(R.string.login_pref));

        if(lastLoginDate!=null) {
            isLoginNeeded = Helper.compareDates(lastLoginDate, minsInDay);
            if (!isLoginNeeded)
                openMainPage();
            else
                Helper.showMessage(PinLoginActivity.this, getString(R.string.login_again), getString(R.string.login_message_timeout),
                        MotionToast.TOAST_WARNING);
        }
    }

    private void openMainPage(){
        // if user logs in for the first time and device has fingerprint hardware
        // next time they log in, they have option to use fingerprint instead
        if(isFingerprintWorking() && Helper.getPreference(context, getString(R.string.fingerprint_pref)) == null)
            Helper.savePreference(context, getString(R.string.fingerprint_enabled), getString(R.string.fingerprint_pref));

        // saves current time of login if logging in for the first time or if it has been 24 hours since last login
        if(lastLoginDate == null || isLoginNeeded)
            Helper.savePreference(context, String.valueOf(Calendar.getInstance().getTimeInMillis()), getString(R.string.login_pref));

        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }

}