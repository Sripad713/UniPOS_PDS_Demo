package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.DealerDetailsActivity;
import com.visiontek.Mantra.Activities.UIDDetailsActivity;
import com.visiontek.Mantra.Models.DATAModels.AadhaarSeedingListModel;
import com.visiontek.Mantra.Models.DATAModels.DealerListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AadharSeedingListAdapter extends RecyclerView.Adapter<AadharSeedingListAdapter.MyViewHolder> {


    Context context;
    private final ArrayList<AadhaarSeedingListModel> dataSet;
    private final UIDDetailsActivity.OnClickUID OnClickUID;

    public AadharSeedingListAdapter(Context context, ArrayList<AadhaarSeedingListModel> data,
                             UIDDetailsActivity.OnClickUID OnClickUID) {
        this.dataSet = data;
        this.context = context;
        this.OnClickUID = OnClickUID;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item3_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final AadhaarSeedingListModel model = dataSet.get(listPosition);

        LinearLayout lin = holder.linearLayout;
        TextView textViewName = holder.textName;
        TextView textViewUid = holder.textUid;
        TextView textViewStatus = holder.textStatus;

        textViewName.setText(dataSet.get(listPosition).getName());
        textViewUid.setText(dataSet.get(listPosition).getUid());
        textViewStatus.setText(dataSet.get(listPosition).getStatus());

        if (model.isSelected) {
            lin.setBackground(context.getResources().getDrawable(R.drawable.bgreen));
            // lin.setBackgroundColor(context.getResources().getColor(R.color.green));
        } else {
            lin.setBackground(context.getResources().getDrawable(R.drawable.blightblue));
            //lin.setBackgroundColor(context.getResources().getColor(R.color.background));
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < dataSet.size(); i++) {
                    dataSet.get(i).isSelected = false;
                }
                model.isSelected = true;

                OnClickUID.onClick(listPosition);

                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textName;
        TextView textUid;
        TextView textStatus;
        LinearLayout linearLayout;

        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textName = itemView.findViewById(R.id.A);
            this.textUid = itemView.findViewById(R.id.B);
            this.textStatus = itemView.findViewById(R.id.C);

        }
    }
}

