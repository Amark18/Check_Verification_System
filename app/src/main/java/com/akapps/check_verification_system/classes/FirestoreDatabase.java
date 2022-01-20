package com.akapps.check_verification_system.classes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import com.akapps.check_verification_system.activities.MainActivity;
import com.akapps.check_verification_system.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import www.sanju.motiontoast.MotionToast;

public class FirestoreDatabase {

    // activity
    private final Activity currentActivity;
    private final Context context;

    // fire-store database
    private ArrayList<Customer> customers;
    private final FirebaseFirestore db;
    private final CollectionReference collectionCustomers;

    // storage database
    private final FirebaseStorage storage;

    // variables
    private final String profilePicturePath  = "_profilePic";
    private final String idPicturePath  = "_idPic";
    private String storeName;

    // layout
    private Dialog progressDialog;
    private MaterialCardView internetWarningColor;
    private TextView internetMessage;

    public FirestoreDatabase(Activity activity, Context context){
        this.currentActivity = activity;
        this.context = context;
        this.internetWarningColor = activity.findViewById(R.id.internet_connected);
        internetWarningColor.getLayoutParams().width = 3 * Helper.getWidthScreen(activity) / 4;
        this.internetMessage = activity.findViewById(R.id.internet_message);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        collectionCustomers = db.collection(context.getString(R.string.database_main_collection));
        customers = new ArrayList<>();
        storeName = Helper.getStoreName(context);
    }

    public ArrayList<Customer> getCustomers(){
        return customers;
    }

    public CollectionReference getCustomersCollectionRef(){
        return collectionCustomers;
    }

    public void loadCustomerData(boolean updateRecyclerview){
        progressDialog = Helper.showLoading(progressDialog, context, true);
        Dialog finalProgressDialog = progressDialog;
        String lastUpdated = Helper.getPreference(context, context.getString(R.string.last_update_pref));
        // when app is first used, all customers are loaded
        if(lastUpdated == null) {
            collectionCustomers
                    .orderBy("timeStampAdded", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                                customers.add(document.toObject(Customer.class));
                            Helper.savePreference(context, Helper.getCurrentDate(), context.getString(R.string.last_update_pref));
                            ((MainActivity) context).updateLayoutData(customers, updateRecyclerview);
                        } else {
                            Helper.showMessage(currentActivity, context.getString(R.string.database_error_title),
                                    context.getString(R.string.database_error),
                                    MotionToast.TOAST_ERROR);
                        }
                        Helper.showLoading(finalProgressDialog, context, false);
                    });
        }
        else{
            // gets current list from cache (for when closing and opening app again)
            collectionCustomers
                    .orderBy("timeStampAdded", Query.Direction.DESCENDING)
                    .get(Source.CACHE)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            customers = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult())
                                    customers.add(document.toObject(Customer.class));
                            // gets new customers from database between last database call and now
                            collectionCustomers
                                    .whereGreaterThan(context.getString(R.string.field_timeStampAdded), lastUpdated)
                                    .get(Source.SERVER)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task2.getResult()) {
                                                Customer currentItem = document.toObject(Customer.class);
                                                if (!customerExists(currentItem.getCustomerUniqueId()))
                                                    customers.add(currentItem);
                                            }
                                            Helper.savePreference(context, Helper.getCurrentDate(), context.getString(R.string.last_update_pref));
                                            ((MainActivity) context).updateLayoutData(customers, updateRecyclerview);
                                            checkConnection(true);
                                        } else {
                                            checkConnection(false);
                                            ((MainActivity) context).updateLayoutData(customers, updateRecyclerview);
                                            Helper.showMessage(currentActivity, context.getString(R.string.database_error_title),
                                                    context.getString(R.string.database_error),
                                                    MotionToast.TOAST_ERROR);
                                        }
                                        Helper.showLoading(finalProgressDialog, context, false);
                                    });
                        } else
                            checkConnection(false);
                        Helper.showLoading(finalProgressDialog, context, false);
                    });
        }
    }

    // adds customer to database
    public void addCustomer(String firstName, String lastName, int dobYear, String phoneNumber, String profilePicPath, String customerIDPath){
        String customerID = firstName.charAt(0) + lastName + "" + dobYear;
        Customer addCustomer = new Customer(firstName, lastName, dobYear, phoneNumber, customerID,
                Helper.getDatabaseDate(), "", profilePicPath, customerIDPath,
                Helper.getCurrentDate(), Helper.getStoreName(context));
        collectionCustomers.document(customerID).set(addCustomer);
    }

    // finds all customers matching query and populates recyclerview for user to see
    public void searchForCustomer(String query){
        query = query.toLowerCase();
        String finalQuery = query;

        // searches for customer using their name/year of birth/phone number
        ArrayList<Customer> queryCustomers = (ArrayList<Customer>) customers.stream().filter(customer ->
                customer.getFirstName().toLowerCase().contains(finalQuery) ||
                        (null != customer.getPhoneNumber() && customer.getPhoneNumber().contains(finalQuery.replace(" ", ""))) ||
                        customer.getLastName().toLowerCase().contains(finalQuery) ||
                        (customer.getFirstName().toLowerCase() + " " +
                                customer.getLastName().toLowerCase()).contains(finalQuery) ||
                        customer.getCustomerUniqueId().toLowerCase().contains(finalQuery))
                .collect(Collectors.toList());
        ((MainActivity) context).populateRecyclerview(queryCustomers);
    }

    // updates customer string data to database
    public void updateCustomer(String customerID, String field, String updatedValue){
        collectionCustomers.document(customerID).update(field, updatedValue);
    }

    // when viewing a customer, check to see if check cashing status of customer has changed/
    // their verification history/their phone number and if so, update the local copy with the live copy
    public boolean updateLocalCustomer(Customer localCustomer, Customer liveCustomer){
        if(!(getVerificationHistoryString(liveCustomer).equals(getVerificationHistoryString(localCustomer))) ||
            liveCustomer.isDoNotCash() != localCustomer.isDoNotCash() ||
                !liveCustomer.getPhoneNumber().equals(localCustomer.getPhoneNumber()) ) {
            // reflect changes for user to see
            loadCustomerData(true);
            return true;
        }
        return false;
    }

    private String getVerificationHistoryString(Customer customer){
        return customer.getVerificationHistory().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    // updates customer checking status to database and updates UI to reflect change
    public void updateCustomerStatus(String customerID, String field, boolean updatedValue){
        collectionCustomers.document(customerID).update(field, updatedValue);
        // reflect changes for user to see
        loadCustomerData(true);
    }

    // deletes customer from server and cache
    public void deleteCustomer(Customer customer){
        // deleted customer data, his profile picture, and ID picture
        String profileImagePath = customer.getProfilePicPath();
        String idImagePath = customer.getCustomerIDPath();
        // profile image path is optional and so we can't delete something if it may not exist
        if(!profileImagePath.isEmpty())
            deleteImage(profileImagePath);
        deleteImage(idImagePath);
        collectionCustomers.document(customer.getCustomerUniqueId()).delete();
        // reflect changes for user to see
        loadCustomerData(true);
    }

    // determines if customer exists in database
    public boolean customerExists(String customerId) {
        ArrayList<Customer> exists = (ArrayList<Customer>) customers.stream().filter(customer ->
                customer.getCustomerUniqueId().toLowerCase().contains(customerId.toLowerCase()))
                .collect(Collectors.toList());

        return exists.size() == 1;
    }

    public void addHistoryAndDateVerified(Customer customer){
        // combines the two updates into one to reduce number of writes to database
        WriteBatch batch = db.batch();
        DocumentReference customerRef = collectionCustomers.document(customer.getCustomerUniqueId());
        ArrayList<VerificationHistory> history = getSortedHistory(customer);
        // history is only updated if there is a 1 min difference between updates
        if(history.size() == 0 || Helper.compareDates(Helper.convertStringDateToMilli(history.get(0).getDateVerified()), 1)){
            batch.update(customerRef, context.getString(R.string.field_verificationHistory),
                    FieldValue.arrayUnion(new VerificationHistory(Helper.getCurrentDate(),
                            storeName)));
        }

        if(!customer.getDateVerified().equals(Helper.getDatabaseDate()))
            batch.update(customerRef, context.getString(R.string.field_verifiedToday), Helper.getDatabaseDate());

        batch.commit().addOnCompleteListener(task -> { });
        loadCustomerData(false);
    }

    public ArrayList<VerificationHistory> getSortedHistory(Customer customer){
        ArrayList<VerificationHistory> history = (ArrayList<VerificationHistory>) customer.getVerificationHistory();
        history.sort((o1, o2) -> {
            DateFormat f = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm:ss a");
            try {
                return f.parse(o2.getDateVerified()).compareTo(f.parse(o1.getDateVerified()));
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        });
        return history;
    }

    public ArrayList<Customer> getVerifiedTodayList(){
        return  (ArrayList<Customer>) customers.stream().filter(customer ->
                customer.getDateVerified().contains(Helper.getDatabaseDate())).collect(Collectors.toList());
    }

    public ArrayList<Customer> getAddedTodayList(){
        return  (ArrayList<Customer>) customers.stream().filter(customer ->
                customer.getDateAdded().contains(Helper.getDatabaseDate())).collect(Collectors.toList());
    }

    public ArrayList<Customer> getViewedTodayList(){
        return  (ArrayList<Customer>) customers.stream().filter(customer ->
                customer.getDateViewed().contains(Helper.getDatabaseDate())).collect(Collectors.toList());
    }

    public void checkConnection(boolean status){
        if(status) {
            internetWarningColor.setVisibility(View.GONE);
            internetMessage.setVisibility(View.GONE);
        }
        else {
            internetWarningColor.setVisibility(View.VISIBLE);
            internetMessage.setVisibility(View.VISIBLE);
        }
    }

    public void uploadImages(Uri profileImageUri, Uri idImageUri, Customer customer){
        int uniqueID = new Random().nextInt(1001) + 200;
        String profileImagePath = customer.getCustomerUniqueId() + profilePicturePath + "_" + uniqueID +  ".jpg";
        String idImagePath = customer.getCustomerUniqueId() + idPicturePath + "_" + uniqueID + ".jpg";

        // uploads profile picture
        if(profileImageUri != null) {
            // delete old profile image
            if(!customer.getProfilePicPath().isEmpty())
                deleteImage(customer.getProfilePicPath());
            StorageReference profileImageRef = storage.getReference(profileImagePath);
            UploadTask uploadProfileImage = profileImageRef.putFile(profileImageUri);
            upLoadImage(uploadProfileImage, profileImagePath, customer.getCustomerUniqueId());
        }

        // upload identification
        if(idImageUri != null) {
            // delete old id image
            if(!customer.getCustomerIDPath().isEmpty())
                deleteImage(customer.getCustomerIDPath());
            StorageReference idImageRef = storage.getReference(idImagePath);
            UploadTask uploadIdImage = idImageRef.putFile(idImageUri);
            upLoadImage(uploadIdImage, idImagePath, customer.getCustomerUniqueId());
        }
    }

    private void upLoadImage(UploadTask uploadTask, String imagePath, String customerID){
        // if image is uploaded successfully, also update their image path in database
        uploadTask.addOnFailureListener(exception ->
                Helper.showMessage(currentActivity, context.getString(R.string.upload_error_title),
                context.getString(R.string.upload_error_message), MotionToast.TOAST_ERROR))
                .addOnSuccessListener(taskSnapshot -> {
                    if(imagePath.contains(idPicturePath))
                        updateCustomer(customerID, context.getString(R.string.field_customerIDPath), imagePath);
                    else {
                        updateCustomer(customerID, context.getString(R.string.field_profilePicPath), imagePath);
                        // reflect changes for user to see
                        loadCustomerData(true);
                    }
                });
    }

    private void deleteImage(String imagePath){
        StorageReference imagePathRef = storage.getReference(imagePath);

        imagePathRef.delete().addOnSuccessListener(aVoid -> { })
                .addOnFailureListener(exception -> {});
    }
}
