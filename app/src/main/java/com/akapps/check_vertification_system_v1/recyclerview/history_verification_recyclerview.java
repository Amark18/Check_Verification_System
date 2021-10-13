package com.akapps.check_vertification_system_v1.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.check_vertification_system_v1.R;
import com.akapps.check_vertification_system_v1.classes.Helper;
import com.akapps.check_vertification_system_v1.classes.VerificationHistory;
import java.util.ArrayList;

public class history_verification_recyclerview extends RecyclerView.Adapter<history_verification_recyclerview.MyViewHolder>{

    // project data
    private ArrayList<VerificationHistory> history;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView store;
        private final TextView timeElapsed;
        private final View view;

        public MyViewHolder(View v) {
            super(v);
            date = v.findViewById(R.id.history_date);
            store = v.findViewById(R.id.store);
            timeElapsed = v.findViewById(R.id.time_elapsed);
            view = v;
        }
    }

    public history_verification_recyclerview(ArrayList<VerificationHistory> history) {
        this.history = history;
    }

    @Override
    public history_verification_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_history_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // retrieves current history object
        VerificationHistory currentHistory = history.get(position);
        String timeAgo = Helper.getTimeDifference(Helper.convertStringDateToCalender(currentHistory.getDateVerified())) +
                " " + holder.view.getContext().getString(R.string.ago_text);

        holder.date.setText(currentHistory.getDateVerified());
        holder.store.setText(currentHistory.getStoreName());
        holder.timeElapsed.setText("~ " + timeAgo);
    }

    @Override
    public int getItemCount() {
        return history.size();
    }
}
