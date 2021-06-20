package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Models.DATAModels.DataModel3;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomAdapter3 extends RecyclerView.Adapter<CustomAdapter3.MyViewHolder> {
    private final List<DataModel3> dataSet;

    public CustomAdapter3(Context context, List<DataModel3> data) {
        this.dataSet = data;
    }

    @NotNull
    @Override
    public CustomAdapter3.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item5_row, parent, false);
        return new CustomAdapter3.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        DataModel3 data3 = dataSet.get(position);
        holder.A.setText(data3.a());
        holder.B.setText(data3.b());
        holder.C.setText(data3.c());
        holder.D.setText(data3.d());
        holder.E.setText(data3.e());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView A;
        TextView B;
        TextView C;
        TextView D;
        TextView E;

        MyViewHolder(View itemView) {
            super(itemView);
            this.A = itemView.findViewById(R.id.A);
            this.B = itemView.findViewById(R.id.B);
            this.C = itemView.findViewById(R.id.C);
            this.D = itemView.findViewById(R.id.D);
            this.E = itemView.findViewById(R.id.E);
        }
    }
}
