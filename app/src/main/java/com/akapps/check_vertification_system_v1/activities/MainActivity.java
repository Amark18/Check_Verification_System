package com.akapps.check_vertification_system_v1.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import com.akapps.check_vertification_system_v1.R;
import com.akapps.check_vertification_system_v1.classes.Animation;
import com.akapps.check_vertification_system_v1.classes.BottomSheetHelper;
import com.akapps.check_vertification_system_v1.classes.Customer;
import com.akapps.check_vertification_system_v1.classes.FirestoreDatabase;
import com.akapps.check_vertification_system_v1.classes.Helper;
import com.akapps.check_vertification_system_v1.classes.NFC;
import com.akapps.check_vertification_system_v1.recyclerview.customers_search_recyclerview;
import com.google.android.material.card.MaterialCardView;
import com.irfaan008.irbottomnavigation.SpaceItem;
import com.irfaan008.irbottomnavigation.SpaceNavigationView;
import com.irfaan008.irbottomnavigation.SpaceOnClickListener;
import java.util.ArrayList;
import java.util.stream.Collectors;
import www.sanju.motiontoast.MotionToast;

public class MainActivity extends AppCompatActivity {

    // NFC
    private NFC nfc;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    // activity
    private Context context;

    // bottom navigation
    private SpaceNavigationView spaceNavigationView;

    // animation
    private Animation animation;

    // layout
    private TextView date;
    private LinearLayout dash;
    private SearchView searchView;
    private TextView closeSearch;
    private ImageView nfcStatus;
    private ImageView settings;
    private RecyclerView customerRecyclerview;
    private RecyclerView.Adapter adapterCustomers;
    private LinearLayout searchLayout;
    private TextView totalInSystemText;
    private MaterialCardView totalInSystemCardView;
    private TextView addedTodayText;
    private MaterialCardView addedTodayCardView;
    private TextView verifiedTodayText;
    private MaterialCardView verifiedTodayCardView;
    private TextView emptyRecyclerviewMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    // variables
    private final int longAnimationDuration = 800;
    private boolean writeNfcMode = false;
    private boolean readNfcMode = false;
    private String writeString = "";
    public BottomSheetHelper bottomSheetHelper;
    private boolean showKeyboard = true;

    // database
    private ArrayList<Customer> customers;
    public FirestoreDatabase firestoreDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // locks orientation
        Helper.setOrientation(this, context);

        initializeLayout(savedInstanceState);

        // ensures sheets only have one instance
        bottomSheetHelper = new BottomSheetHelper(this);
        initializeNFC();

        // initialize animation
        animation = new Animation(searchView, closeSearch, nfcStatus, searchLayout,
                customerRecyclerview, emptyRecyclerviewMessage, nfcAdapter, settings);

        // initialize Database
        firestoreDatabase = new FirestoreDatabase(this, context);
        firestoreDatabase.loadCustomerData(false);
    }

    @Override
    public void onBackPressed() {
        // if search bar is open, close it on back press
        if(searchView.getVisibility() == View.VISIBLE)
            closeSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // re-enables nfc and checks to see if nfc status has been updated by user
        if (nfcAdapter != null) {
            nfc.checkNfcStatus();
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // closes nfc
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bottomSheetHelper != null)
            bottomSheetHelper.closeAllSheets();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // catches nfc tap by user when app is open
        // reads nfc card to open customer
        if(nfc != null && !writeNfcMode && !readNfcMode) {
            nfc.readNFCTag(intent, false, "");
            bottomSheetHelper.closeNfcSheet();
        }
        // writes to nfc card
        else if(nfc != null && writeNfcMode){
            nfc.readNFCTag(intent, true, writeString);
            writeNfcMode = false;
        }
        // read card to output card data to user
        else if(nfc != null && readNfcMode){
            nfc.readNFCTag(intent, false, getString(R.string.output_data));
            readNfcMode = false;
            bottomSheetHelper.closeNfcSheet();
        }
    }

    private void initializeNFC(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            return;
        }
        pendingIntent = PendingIntent.getActivity(context, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfc = new NFC(this, context, nfcAdapter, pendingIntent, nfcStatus, bottomSheetHelper);
    }

    public void showNfcPrompt(String customerId, boolean readNfcMode){
        if(readNfcMode)
            this.readNfcMode = true;
        else{
            writeNfcMode = true;
            writeString = customerId;
        }
        if(nfc != null)
            nfc.showNfcPrompt();
    }

    private void initializeLayout(Bundle savedInstanceState){
        date = findViewById(R.id.date);
        dash = findViewById(R.id.dashboard);
        settings = findViewById(R.id.settings);
        totalInSystemText = findViewById(R.id.num_total);
        totalInSystemCardView = findViewById(R.id.total_layout);
        addedTodayText = findViewById(R.id.num_added);
        addedTodayCardView = findViewById(R.id.added_layout);
        verifiedTodayText = findViewById(R.id.num_verified);
        verifiedTodayCardView = findViewById(R.id.verified_layout);
        searchLayout = findViewById(R.id.search_layout_main);
        searchView = findViewById(R.id.searchview);
        spaceNavigationView = findViewById(R.id.space);
        closeSearch = findViewById(R.id.close_search);
        nfcStatus = findViewById(R.id.nfc_icon);
        customerRecyclerview = findViewById(R.id.customers_recyclerview);
        emptyRecyclerviewMessage = findViewById(R.id.recyclerview_empty_text);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);

        // setting up
        customerRecyclerview.setHasFixedSize(true);
        customerRecyclerview.setLayoutManager(new GridLayoutManager(context, 2));
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("home", R.drawable.home_icon));
        spaceNavigationView.addSpaceItem(new SpaceItem("search", R.drawable.search_icon));
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);
        spaceNavigationView.setCentreButtonRippleColor(ContextCompat.getColor(this, R.color.cerulean_blue));
        spaceNavigationView.showIconOnly();
        searchView.setFocusable(false);
        searchView.setIconified(false);
        searchView.clearFocus();
        date.setText(Helper.getTodaysDate());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String typingQuery) {
                if(typingQuery.length() > 0)
                    firestoreDatabase.searchCustomer(typingQuery);
                else
                    populateRecyclerview(new ArrayList<>());
                return false;
            }
        });

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                // if search bar is open, close it
                if(searchView.getVisibility() == View.VISIBLE)
                    closeSearch();
                // opens bottom sheet so user can add a new customer
                bottomSheetHelper.openCustomerSheet(MainActivity.this, null);
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                // search is selected
               if(itemIndex == 1) {
                   animation.slideUp(searchLayout, dash, showKeyboard);
                   firestoreDatabase.loadCustomerData(false);
                   // resets boolean
                   showKeyboard = true;
               }
               // home is selected
               else if(itemIndex == 0)
                   animation.slideDown(searchLayout, dash);
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {}
        });

        // pulling down updates data and updates recyclerview
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // retrieves data from database to see if there is any updates
            firestoreDatabase.loadCustomerData(customerRecyclerview.getAdapter() != null ? true : false);
            swipeRefreshLayout.setRefreshing(false);
        });

        closeSearch.setOnClickListener(v -> {
           closeSearch();
        });

        settings.setOnClickListener(v -> {
            bottomSheetHelper.openSettingsSheet("");
        });

        // checks to see if NFC is not enabled and opens NFC settings
        nfcStatus.setOnClickListener(v -> {
            if(nfc != null)
                nfc.isNfcDisabled();
        });

        totalInSystemCardView.setOnClickListener(v -> {
            showKeyboard = false;
            spaceNavigationView.changeCurrentItem(1);
            new Handler().postDelayed(() -> {
                populateRecyclerview(customers);
            }, 1000);
        });

        addedTodayCardView.setOnClickListener(v -> {
            showKeyboard = false;
            ArrayList<Customer> addedCustomersToday = (ArrayList<Customer>) customers.stream().filter(customer ->
                    customer.getDateAdded().contains(Helper.getDatabaseDate())).collect(Collectors.toList());
            if(addedCustomersToday.size() > 0) {
                spaceNavigationView.changeCurrentItem(1);
                new Handler().postDelayed(() -> {
                    populateRecyclerview(addedCustomersToday);
                }, 1000);
            }
        });

        verifiedTodayCardView.setOnClickListener(v -> {
            showKeyboard = false;
            ArrayList<Customer> verifiedCustomersToday = (ArrayList<Customer>) customers.stream().filter(customer ->
                    customer.getDateVerified().contains(Helper.getDatabaseDate())).collect(Collectors.toList());
            if(verifiedCustomersToday.size() > 0) {
                spaceNavigationView.changeCurrentItem(1);
                new Handler().postDelayed(() -> {
                    populateRecyclerview(verifiedCustomersToday);
                }, 1000);
            }
        });
    }

    public void populateRecyclerview(ArrayList<Customer> customers){
        if(customers.size() == 0) {
            emptyRecyclerviewMessage.setVisibility(View.VISIBLE);
            customerRecyclerview.setAdapter(null);
        }
        else {
            emptyRecyclerviewMessage.setVisibility(View.GONE);
            adapterCustomers = new customers_search_recyclerview(customers, MainActivity.this, context);
            customerRecyclerview.setAdapter(adapterCustomers);
        }
    }

    public void findCustomer(String customerId){
        ArrayList<Customer> result = (ArrayList<Customer>) customers.stream().filter(customer -> customer.getCustomerUniqueId().contains(customerId)).collect(Collectors.toList());

        if(customerId.isEmpty())
            Helper.showMessage(this, context.getString(R.string.read_nfc_title),
                    context.getString(R.string.read_nfc_empty_message),
                    MotionToast.TOAST_ERROR);
        else if(result.size() != 1) {
            Helper.showMessage(this, context.getString(R.string.read_nfc_title),
                    context.getString(R.string.read_nfc_message),
                    MotionToast.TOAST_ERROR);
        }
        else{
            bottomSheetHelper.openCustomerSheet(MainActivity.this, result.get(0));
            firestoreDatabase.addHistoryRecord(result.get(0));
        }
    }

    public void updateLayoutData(ArrayList<Customer> updatedCustomers, boolean updateRecyclerview){
        customers = updatedCustomers;
        String total = "" + customers.size();
        String added = "" + customers.stream().filter(customer ->
                customer.getDateAdded().contains(Helper.getDatabaseDate())).collect(Collectors.toList()).size();
        String verified = "" + customers.stream().filter(customer ->
                customer.getDateVerified().contains(Helper.getDatabaseDate())).collect(Collectors.toList()).size();
        totalInSystemText.setText(total);
        addedTodayText.setText(added);
        verifiedTodayText.setText(verified);
        if(updateRecyclerview)
            populateRecyclerview(customers);
    }

    // closes search by "pressing on" home icon at position 0
    // this will run the spaceNavigationView OnItemClick Method
    // which does the slide down animation for search bar
    private void closeSearch(){
        spaceNavigationView.changeCurrentItem(0);
    }

}