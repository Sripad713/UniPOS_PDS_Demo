package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.InspectionActivity;
import com.visiontek.Mantra.Models.DATAModels.DataModel2;
import com.visiontek.Mantra.R;

import java.util.ArrayList;

public class CustomAdapter6 extends RecyclerView.Adapter<CustomAdapter6.MyViewHolder> {

    ArrayList<DataModel2> dataSet;
    Context context;
    private final InspectionActivity.OnClickListener onClickListener;

    public CustomAdapter6(InspectionActivity context, ArrayList<DataModel2> data, InspectionActivity.OnClickListener onClickListener) {

        this.context = context;
        this.dataSet = data;
        this.onClickListener = onClickListener;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row4, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int listPosition) {

        final DataModel2 model = dataSet.get(listPosition);

        holder.textViewName.setText(dataSet.get(listPosition).getTot());
        holder.textBalance.setText(dataSet.get(listPosition).getBal());
        holder.textObservation.setText(dataSet.get(listPosition).getReq());
        holder.textVariation.setText(dataSet.get(listPosition).getRate());

        LinearLayout lin = holder.linearLayout;

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
                onClickListener.onClick_d(listPosition);
                notifyDataSetChanged();
            }
        });

    }
    @Override
    public int getItemCount() {
        return dataSet.size();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textBalance;
        TextView textObservation;
        TextView textVariation;
        LinearLayout linearLayout;


        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.linear);

            this.textViewName = itemView.findViewById(R.id.total);
            this.textBalance = itemView.findViewById(R.id.bal);
            this.textObservation = itemView.findViewById(R.id.rate);
            this.textVariation = itemView.findViewById(R.id.close);

        }
    }
}


