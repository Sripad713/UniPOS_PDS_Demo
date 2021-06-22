package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.RationDetailsActivity;
import com.visiontek.Mantra.Models.DATAModels.RationListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RationListAdapter extends RecyclerView.Adapter<RationListAdapter.MyViewHolder> {


    Context context;
    private final ArrayList<RationListModel> dataSet;
    private final RationDetailsActivity.OnClickRation OnClickRation;

    public RationListAdapter(Context context, ArrayList<RationListModel> data,
                             RationDetailsActivity.OnClickRation OnClickRation) {
        this.context = context;
        this.dataSet = data;
        this.OnClickRation = OnClickRation;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row6, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final RationListModel model = dataSet.get(listPosition);

        LinearLayout lin = holder.linearLayout;
        TextView textViewName = holder.textName;
        TextView textViewPrice = holder.textPrice;
        TextView textViewBal = holder.textBal;
        TextView textViewClbal = holder.textClbal;
        TextView textViewIssue = holder.textIssue;
        TextView textViewAmount = holder.textAmount;

        textViewName.setText(dataSet.get(listPosition).getName());
        textViewPrice.setText(dataSet.get(listPosition).getPrice());
        textViewBal.setText(dataSet.get(listPosition).getBal());
        textViewClbal.setText(dataSet.get(listPosition).getClbal());
        textViewIssue.setText(dataSet.get(listPosition).getIssue());
        textViewAmount.setText(dataSet.get(listPosition).getAmount());

        if (model.isSelected) {
            lin.setBackground(context.getResources().getDrawable(R.drawable.bgreen));
            //lin.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
            lin.setBackground(context.getResources().getDrawable(R.drawable.white));
            //lin.setBackgroundColor(context.getResources().getColor(R.color.background));
        }

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < dataSet.size(); i++) {
                    dataSet.get(i).isSelected = false;
                }
                model.isSelected = true;

                OnClickRation.onClick(listPosition);

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
        TextView textName;
        TextView textPrice;
        TextView textBal;
        TextView textClbal;
        TextView textIssue;
        TextView textAmount;


        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textName = itemView.findViewById(R.id.A);
            this.textPrice = itemView.findViewById(R.id.B);
            this.textBal = itemView.findViewById(R.id.C);
            this.textClbal = itemView.findViewById(R.id.D);
            this.textIssue = itemView.findViewById(R.id.E);
            this.textAmount = itemView.findViewById(R.id.F);

        }
    }
}

