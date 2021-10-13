package com.akapps.check_vertification_system_v1.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.check_vertification_system_v1.bottomsheet.AddCustomerSheet;
import com.akapps.check_vertification_system_v1.R;
import com.akapps.check_vertification_system_v1.classes.Customer;
import com.akapps.check_vertification_system_v1.classes.Helper;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class customers_search_recyclerview extends RecyclerView.Adapter<customers_search_recyclerview.MyViewHolder>{

    // project data
    private ArrayList<Customer> customers;
    private FragmentActivity activity;
    private Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView customerLayout;
        private final MaterialCardView warningLayoutColor;
        private final ImageView customerImage;
        private final TextView customerFullName;
        private final TextView customerYear;
        private final View view;

        public MyViewHolder(View v) {
            super(v);
            customerLayout = v.findViewById(R.id.customer_id);
            customerImage = v.findViewById(R.id.customer_image);
            customerFullName = v.findViewById(R.id.customer_name);
            customerYear = v.findViewById(R.id.customer_year);
            warningLayoutColor = v.findViewById(R.id.warningColor);
            view = v;
        }
    }

    public customers_search_recyclerview(ArrayList<Customer> customers, FragmentActivity activity, Context context) {
        this.customers = customers;
        this.activity = activity;
        this.context = context;
    }

    @Override
    public customers_search_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_customer_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // retrieves current customer object
        Customer currentCustomer= customers.get(position);

        holder.customerFullName.setText(currentCustomer.getFirstName() + " " + currentCustomer.getLastName());
        holder.customerYear.setText(String.valueOf(currentCustomer.getDobYear()));

        holder.warningLayoutColor.getLayoutParams().width = Helper.getWidthScreen(activity) / 4;
        // if customer is set to do not cash, there is an indicator above their photo to reflect that
        if(currentCustomer.isDoNotCash())
            holder.warningLayoutColor.setVisibility(View.VISIBLE);
        else
            holder.warningLayoutColor.setVisibility(View.INVISIBLE);

        // if there is a profile picture saved in database, show customer profile picture
        if(!currentCustomer.getProfilePicPath().isEmpty()) {
            StorageReference profilePicRef = FirebaseStorage.getInstance().getReference(currentCustomer.getProfilePicPath());
            // gets profile photo from firebase storage
            Glide.with(context)
                    .load(profilePicRef)
                    .circleCrop()
                    .placeholder(context.getDrawable(R.drawable.user_icon))
                    .into(holder.customerImage);
        }

        // if customer layout is clicked, bottom sheet opens with their info
        holder.customerLayout.setOnClickListener(v -> {
            AddCustomerSheet addCustomer = new AddCustomerSheet(currentCustomer, activity, position);
            addCustomer.show(activity.getSupportFragmentManager(), addCustomer.getTag());
        });
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }
}
