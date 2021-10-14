package com.akapps.check_verification_system.classes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.akapps.check_verification_system.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;
import www.sanju.motiontoast.MotionToast;
import static android.content.Context.MODE_PRIVATE;

public class Helper {

    // returns true if device is in landscape
    public static boolean isLandscape(Context context){
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // returns true if device is in portrait
    public static boolean isPortrait(Context context){
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    // locks orientation
    public static void setOrientation(Activity activity, Context context){
        if(isPortrait(context))
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else if(isLandscape(context))
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    // returns width of screen
    public static int getWidthScreen(Activity currentActivity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    // returns height of screen
    public static int getHeightScreen(Activity currentActivity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    // checks current status of nfc (enabled or not by the user) and updates NFC icon to reflect that
    public static boolean checkNfcStatus(Activity activity, Context context){
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            return true;
        }
        else
            Helper.showMessage(activity, context.getString(R.string.nfc_text), context.getString(R.string.nfc_message),
                    MotionToast.TOAST_ERROR);
        return false;
    }

    // returns greeting based on time of day
    public static String timeOfDayGreeting(){
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);

        if(hour < 12)
            return "Good Morning!";
        else if (hour < 17)
            return "Good Afternoon!";
        else
            return "Good Evening!";
    }

    // saves a small piece of data
    public static void savePreference(Context context, String data, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, data);
        editor.apply();
    }

    // retrieved data saved
    public static String getPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app", MODE_PRIVATE);
        String data = sharedPreferences.getString(key, null);
        return data;
    }

    // checks to see if user has logged in via email and password
    public static boolean isAccountLoggedIn(Context context){
        String isLoggedIn = getPreference(context, context.getString(R.string.account_login_pref));
        return isLoggedIn == null ? false : true;
    }

    public static void showMessage(Activity activity, String title, String message, String typeOfMessage){
        MotionToast.Companion.darkColorToast(activity,
                title,
                message,
                typeOfMessage,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(activity , R.font.helvetica_regular));
    }

    public static Dialog showLoading(Dialog progressDialog, Context context, boolean show){
        try {
            if (show) {
                progressDialog = new Dialog(context);
                progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                progressDialog.setContentView(R.layout.custom_dialog_progress);

                TextView progressTv = progressDialog.findViewById(R.id.progress_tv);
                progressTv.setText(context.getResources().getString(R.string.loading));
                progressTv.setTextColor(ContextCompat.getColor(context, R.color.blueDark));
                progressTv.setTextSize(19F);
                if (progressDialog.getWindow() != null)
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                progressDialog.setCancelable(false);
                progressDialog.show();
            } else
                progressDialog.cancel();
        }catch (Exception e){ }
        return progressDialog;
    }

    public static String formatName(String fullName, Context context){
        if(fullName.length() > 0 && fullName.split(" ").length >= 2){
            String[] wordsInName = fullName.split(" ");
            StringBuilder firstName = new StringBuilder();
            String lastName = "";
            for(int i=0; i<wordsInName.length-1; i++){
                firstName.append(wordsInName[i]);
                if(i < wordsInName.length - 2)
                    firstName.append(" ");
            }
            lastName = wordsInName[wordsInName.length-1];
            return firstName + context.getString(R.string.split) + lastName;
        }
        else
            return "";
    }

    // compares two dates to see if they are a certain time a part
    public static boolean compareDates(String datetime, int mins) {
        Calendar now = Calendar.getInstance();
        long differenceInMillis = now.getTimeInMillis() - Long.valueOf(datetime);
        long differenceInMins = (differenceInMillis) / 1000L / 60L; // converts difference to mins
        return (int)differenceInMins >= mins;
    }

    public static void clearAppData(Activity activity) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear " + activity.getApplicationContext().getPackageName() + " HERE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTodaysDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy");
        return sdf.format(c.getTime());
    }

    public static String getDatabaseDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
        return sdf.format(c.getTime());
    }

    public static String getCurrentDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm:ss a");
        return sdf.format(c.getTime());
    }

    public static String convertStringDateToMilli(String date){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return String.valueOf(cal.getTimeInMillis());
    }

    public static Calendar convertStringDateToCalender(String date){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    public static String getStoreName(Context context){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail().replace(context.getString(R.string.email_end_format), "");
            // capitalize fist letter
            return email.substring(0, 1).toUpperCase() + email.substring(1);
        }
        return "Oasis";
    }

    public static String getTimeDifference(Calendar calendar){
        try {
            long secs, mins, hours, days, months, years;
            String timeDifference = "";

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dateSelected = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());

            years = ChronoUnit.YEARS.between(dateSelected, now);
            months = ChronoUnit.MONTHS.between(dateSelected, now);
            secs = ChronoUnit.SECONDS.between(dateSelected, now);
            mins = ChronoUnit.MINUTES.between(dateSelected, now);
            hours = ChronoUnit.HOURS.between(dateSelected, now);
            days = ChronoUnit.DAYS.between(dateSelected, now);

            if (years > 0) {
                timeDifference += years + " year";
                if (years > 1)
                    timeDifference += "s ";
                else
                    timeDifference += " ";
            }
            if (months % 12 > 0) {
                timeDifference += months % 12 + " month";
                if (months % 12 > 1)
                    timeDifference += "s ";
                else
                    timeDifference += " ";
            }
            if (days % 30 > 0) {
                timeDifference += days % 30 + " day";
                if (days % 30 > 1)
                    timeDifference += "s ";
                else
                    timeDifference += " ";
            }
            if (hours % 24 > 0) {
                timeDifference += hours % 24 + " hour";
                if ((hours % 24) > 1)
                    timeDifference += "s ";
                else
                    timeDifference += " ";
            }
            if (mins % 60 > 0) {
                timeDifference += mins % 60 + " minute";
                if ((mins % 60) > 1)
                    timeDifference += "s ";
                else
                    timeDifference += " ";
            }
            if (secs % 60 > 0) {
                timeDifference += secs % 60 + " second";
                if ((secs % 60) > 1)
                    timeDifference += "s";
            }
            return timeDifference;
        }
        catch (Exception e){
            return "";
        }
    }
}
