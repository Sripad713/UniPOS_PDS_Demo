package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.InspectionActivity;
import com.visiontek.Mantra.Activities.RationDetailsActivity;
import com.visiontek.Mantra.Models.DATAModels.InspectionListModel;
import com.visiontek.Mantra.Models.DATAModels.PrintListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InspectionListAdapter extends RecyclerView.Adapter<InspectionListAdapter.MyViewHolder> {

    Context context;
    private final ArrayList<InspectionListModel> dataSet;
    private final InspectionActivity.OnClickInspector OnClickInspector;
    public InspectionListAdapter(Context context, ArrayList<InspectionListModel> data,
                                 InspectionActivity.OnClickInspector OnClickInspector) {
        this.context = context;
        this.dataSet = data;
        this.OnClickInspector=OnClickInspector;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row4, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final InspectionListModel model = dataSet.get(listPosition);
        LinearLayout lin = holder.linearLayout;
        TextView textViewComm = holder.textComm;
        TextView textViewCb = holder.textCb;
        TextView textViewObs = holder.textObs;
        TextView textViewVar = holder.textVar;

        textViewComm.setText(dataSet.get(listPosition).getComm());
        textViewCb.setText(dataSet.get(listPosition).getCb());
        textViewObs.setText(dataSet.get(listPosition).getObs());
        textViewVar.setText(dataSet.get(listPosition).getVar());

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

                OnClickInspector.onClick(listPosition);

                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        TextView textComm;
        TextView textCb;
        TextView textObs;
        TextView textVar;


        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textComm = itemView.findViewById(R.id.A);
            this.textCb = itemView.findViewById(R.id.B);
            this.textObs = itemView.findViewById(R.id.C);
            this.textVar = itemView.findViewById(R.id.D);
        }
    }
}

