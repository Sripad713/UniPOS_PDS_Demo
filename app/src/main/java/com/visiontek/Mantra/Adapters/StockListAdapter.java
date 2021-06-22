package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Models.DATAModels.PrintListModel;
import com.visiontek.Mantra.Models.DATAModels.StockListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.MyViewHolder> {

    Context context;
    private final ArrayList<StockListModel> dataSet;
    public StockListAdapter(Context context, ArrayList<StockListModel> data) {
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
        final StockListModel model = dataSet.get(listPosition);

        TextView textViewComm = holder.textComm;
        TextView textViewScheme = holder.textScheme;
        TextView textViewOb= holder.textOb;
        TextView textViewIssue = holder.textIssue;
        TextView textViewCb = holder.textCb;

        textViewComm.setText(dataSet.get(listPosition).getName());
        textViewScheme.setText(dataSet.get(listPosition).getPrev());
        textViewIssue.setText(dataSet.get(listPosition).getIssue());
        textViewOb.setText(dataSet.get(listPosition).getPrice());
        textViewCb.setText(dataSet.get(listPosition).getAmount());

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        TextView textComm;
        TextView textScheme;
        TextView textOb;
        TextView textIssue;
        TextView textCb;


        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textComm = itemView.findViewById(R.id.A);
            this.textScheme = itemView.findViewById(R.id.B);
            this.textOb = itemView.findViewById(R.id.C);
            this.textIssue = itemView.findViewById(R.id.D);
            this.textCb = itemView.findViewById(R.id.E);
        }
    }
}

