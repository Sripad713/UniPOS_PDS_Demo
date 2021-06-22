package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Models.DATAModels.DailySalesListModel;
import com.visiontek.Mantra.Models.DATAModels.PrintListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DailySalesListAdapter extends RecyclerView.Adapter<DailySalesListAdapter.MyViewHolder> {

    Context context;
    private final ArrayList<DailySalesListModel> dataSet;
    public DailySalesListAdapter(Context context, ArrayList<DailySalesListModel> data) {
        this.context = context;
        this.dataSet = data;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item3_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final DailySalesListModel model = dataSet.get(listPosition);

        TextView textViewComm = holder.textComm;
        TextView textViewScheme = holder.textScheme;
        TextView textViewTotal = holder.textTotal;

        textViewComm.setText(dataSet.get(listPosition).getComm());
        textViewScheme.setText(dataSet.get(listPosition).getScheme());
        textViewTotal.setText(dataSet.get(listPosition).getTotal());

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        TextView textComm;
        TextView textScheme;
        TextView textTotal;


        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textComm = itemView.findViewById(R.id.A);
            this.textScheme = itemView.findViewById(R.id.B);
            this.textTotal = itemView.findViewById(R.id.C);
        }
    }
}
