package com.akapps.check_vertification_system_v1.classes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import com.akapps.check_vertification_system_v1.activities.MainActivity;
import com.akapps.check_vertification_system_v1.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;
import www.sanju.motiontoast.MotionToast;

public class FirestoreDatabase {

    // activity
    private Activity currentActivity;
    private Context context;

    // fire-store database
    private ArrayList<Customer> customers = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference collectionCustomers;

    // storage database
    private FirebaseStorage storage;

    // variables
    private final String profilePicturePath  = "_profilePic";
    private final String idPicturePath  = "_idPic";
    private final String storeName = "Oasis";

    // layout
    private Dialog progressDialog;

    public FirestoreDatabase(Activity activity, Context context){
        this.currentActivity = activity;
        this.context = context;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        collectionCustomers = db.collection(context.getString(R.string.database_main_collection));
    }

    public void loadCustomerData(boolean updateRecyclerview){
        progressDialog = Helper.showLoading(progressDialog, context, true);
        Dialog finalProgressDialog = progressDialog;
        customers = new ArrayList<>();
        collectionCustomers
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult())
                            customers.add(document.toObject(Customer.class));
                        ((MainActivity) context).updateLayoutData(customers, updateRecyclerview);
                    }
                    else
                        Helper.showMessage(currentActivity, context.getString(R.string.database_error_title),
                                context.getString(R.string.database_error),
                                MotionToast.TOAST_ERROR);
                    Helper.showLoading(finalProgressDialog, context, false);
                });
    }

    public void addCustomer(String firstName, String lastName, int dobYear, String profilePicPath, String customerIDPath){
        String customerID = firstName.charAt(0) + lastName + "" + dobYear;
        Customer addCustomer = new Customer(firstName, lastName, dobYear, customerID,
                Helper.getDatabaseDate(), "", profilePicPath, customerIDPath);
        collectionCustomers.document(customerID).set(addCustomer);
    }

    public void searchCustomer(String query){
        query = query.toLowerCase();
        String finalQuery = query;
        
        ArrayList<Customer> queryCustomers = (ArrayList<Customer>) customers.stream().filter(customer ->
                customer.getFirstName().toLowerCase().contains(finalQuery) ||
                        customer.getLastName().toLowerCase().contains(finalQuery) ||
                        (customer.getFirstName().toLowerCase() + " " +
                                customer.getLastName().toLowerCase()).contains(finalQuery)||
                        customer.getCustomerUniqueId().toLowerCase().contains(finalQuery))
                .collect(Collectors.toList());
        ((MainActivity) context).populateRecyclerview(queryCustomers);
    }

    public void updateCustomer(String customerID, String field, String updatedValue){
        collectionCustomers.document(customerID).update(field, updatedValue);
    }

    public void updateCustomerStatus(String customerID, String field, boolean updatedValue, int positionInList){
        collectionCustomers.document(customerID).update(field, updatedValue);
        // positionInList is -1 only when using NFC card and it opens customer info
        // But if searching, then this value is the position of customer in recyclerview
        if(positionInList != -1)
            ((MainActivity) context).updateCustomerWarningStatus(updatedValue, positionInList);
    }

    public void deleteCustomer(String customerID){
        // deleted customer data, his profile picture, and ID picture
        String profileImagePath = customerID + profilePicturePath + ".jpg";
        String idImagePath = customerID + idPicturePath + ".jpg";
        deleteImage(profileImagePath);
        deleteImage(idImagePath);
        collectionCustomers.document(customerID).delete();
    }

    // determines if customer exists in database
    public boolean customerExists(String customerId) {
        ArrayList<Customer> exists = (ArrayList<Customer>) customers.stream().filter(customer ->
                customer.getCustomerUniqueId().toLowerCase().contains(customerId.toLowerCase()))
                .collect(Collectors.toList());

        return exists.size() == 1;
    }

    public void addHistoryRecord(Customer customer){
        ArrayList<VerificationHistory> history = getSortedHistory(customer);
        if(history.size() == 0 || Helper.compareDates(Helper.convertStringDateToMilli(history.get(0).getDateVerified()), 1)){
            collectionCustomers.document(customer.getCustomerUniqueId()).update("verificationHistory",
                    FieldValue.arrayUnion(new VerificationHistory(Helper.getVerificationDate(), storeName)));
            loadCustomerData(false);
        }
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

    public void uploadImages(Uri profileImageUri, Uri idImageUri, String customerID){
        int uniqueID = new Random().nextInt(1001) + 200;
        String profileImagePath = customerID + profilePicturePath + "_" + uniqueID +  ".jpg";
        String idImagePath = customerID + idPicturePath + ".jpg";

        // uploads profile picture
        if(profileImageUri != null) {
            StorageReference profileImageRef = storage.getReference(profileImagePath);
            UploadTask uploadProfileImage = profileImageRef.putFile(profileImageUri);
            upLoadImage(uploadProfileImage, profileImagePath, customerID);
        }

        // upload identification
        if(idImageUri != null) {
            StorageReference idImageRef = storage.getReference(idImagePath);
            UploadTask uploadIdImage = idImageRef.putFile(idImageUri);
            upLoadImage(uploadIdImage, idImagePath, customerID);
        }
    }

    private void upLoadImage(UploadTask uploadTask, String imagePath, String customerID){
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception ->
                Helper.showMessage(currentActivity, context.getString(R.string.upload_error_title),
                context.getString(R.string.upload_error_message), MotionToast.TOAST_ERROR))
                .addOnSuccessListener(taskSnapshot -> {
                    if(imagePath.contains(idPicturePath))
                        updateCustomer(customerID, context.getString(R.string.field_customerIDPath), imagePath);
                    else
                        updateCustomer(customerID, context.getString(R.string.field_profilePicPath), imagePath);
        });
    }

    private void deleteImage(String imagePath){
        StorageReference imagePathRef = storage.getReference(imagePath);

        imagePathRef.delete().addOnSuccessListener(aVoid -> { })
                .addOnFailureListener(exception -> {});
    }
}
