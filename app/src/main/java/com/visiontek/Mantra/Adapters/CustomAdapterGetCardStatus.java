package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Models.DATAModels.DataModel1;
import com.visiontek.Mantra.R;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapterGetCardStatus extends RecyclerView.Adapter<CustomAdapterGetCardStatus.MyViewHolder> {


    private List<DataModel1> dataSet;
    private Context context;
    private  int type;

    public CustomAdapterGetCardStatus(Context context, ArrayList<DataModel1> data) {

        this.dataSet = data;
        this.context = context;
        //this.onClickListener = onClickListener;
        this.type = type;
    }




    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return null;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row5, parent, false);
        return new MyViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final DataModel1 data1 = dataSet.get(position);
        holder.Tot.setText(data1.getTot());
        holder.Bal.setText(data1.getBal());
        holder.Rate.setText(data1.getRate());
        holder.Issue.setText(data1.getReq());
        holder.Close.setText(data1.getClose());


    }

    @Override
    public int getItemCount() {
        //return 0;
        return dataSet.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Tot;
        TextView Bal;
        TextView Rate;
        TextView Issue;
        TextView Close;
        LinearLayout linearLayout;

        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.linear);
            this.Tot = itemView.findViewById(R.id.A);
            this.Bal = itemView.findViewById(R.id.B);
            this.Rate = itemView.findViewById(R.id.C);
            this.Issue = itemView.findViewById(R.id.D);
            this.Close = itemView.findViewById(R.id.E);

        }
    }
}