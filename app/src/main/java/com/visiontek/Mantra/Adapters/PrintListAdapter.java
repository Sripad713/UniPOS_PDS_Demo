package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.RationDetailsActivity;
import com.visiontek.Mantra.Models.DATAModels.PrintListModel;
import com.visiontek.Mantra.Models.DATAModels.RationListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PrintListAdapter extends RecyclerView.Adapter<PrintListAdapter.MyViewHolder> {

    Context context;
    private final ArrayList<PrintListModel> dataSet;
    public PrintListAdapter(Context context, ArrayList<PrintListModel> data) {
        this.context = context;
        this.dataSet = data;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item5_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewName = holder.textName;
        TextView textViewPrev = holder.textPrev;
        TextView textViewIssue = holder.textIssue;
        TextView textViewPrice = holder.textPrice;
        TextView textViewAmount = holder.textAmount;

        textViewName.setText(dataSet.get(listPosition).getName());
        textViewPrev.setText(dataSet.get(listPosition).getPrev());
        textViewIssue.setText(dataSet.get(listPosition).getIssue());
        textViewPrice.setText(dataSet.get(listPosition).getPrice());
        textViewAmount.setText(dataSet.get(listPosition).getAmount());

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        TextView textName;
        TextView textPrev;
        TextView textIssue;
        TextView textPrice;
        TextView textAmount;


        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textName = itemView.findViewById(R.id.A);
            this.textPrev = itemView.findViewById(R.id.B);
            this.textIssue = itemView.findViewById(R.id.C);
            this.textPrice = itemView.findViewById(R.id.D);
            this.textAmount = itemView.findViewById(R.id.E);
        }
    }
}

