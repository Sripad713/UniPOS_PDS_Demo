package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.DeviceInfoActivity;
import com.visiontek.Mantra.Activities.MemberDetailsActivity;
import com.visiontek.Mantra.Models.DATAModels.DeviceInfoListModel;
import com.visiontek.Mantra.Models.DATAModels.MemberListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DeviceInfoListAdapter extends RecyclerView.Adapter<DeviceInfoListAdapter.MyViewHolder> {


    Context context;
    private final ArrayList<DeviceInfoListModel> dataSet;


    public DeviceInfoListAdapter(Context context, ArrayList<DeviceInfoListModel> data) {
        this.context = context;
        this.dataSet = data;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item2_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewName = holder.textName;
        TextView textViewUid = holder.textUid;

        textViewName.setText(dataSet.get(listPosition).getName());
        textViewUid.setText(dataSet.get(listPosition).getUid());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textName;
        TextView textUid;
        LinearLayout linearLayout;

        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textName = itemView.findViewById(R.id.A);
            this.textUid = itemView.findViewById(R.id.B);

        }
    }
}

