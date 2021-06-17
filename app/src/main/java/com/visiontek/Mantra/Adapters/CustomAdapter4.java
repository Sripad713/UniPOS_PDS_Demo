package com.visiontek.Mantra.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.DealerDetailsActivity;
import com.visiontek.Mantra.Activities.MemberDetailsActivity;
import com.visiontek.Mantra.Models.DATAModels.DataModel5;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class CustomAdapter4 extends RecyclerView.Adapter<CustomAdapter4.MyViewHolder> {

    int type;
    Context context;
    int type1;
    private final ArrayList<DataModel5> dataSet;
    private MemberDetailsActivity.OnClickListener onClickListener;
    private final DealerDetailsActivity.OnClickListener onClick;

    public CustomAdapter4(Context context, ArrayList<DataModel5> data, DealerDetailsActivity.OnClickListener onClickListener, int type1) {
        this.dataSet = data;
        this.context = context;
        this.type1 = type1;
        this.onClick = onClickListener;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item3_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final DataModel5 model = dataSet.get(listPosition);
        TextView textViewName = holder.textName;
        TextView textViewUid = holder.textUid;
        TextView textViewStatus = holder.textStatus;
        LinearLayout lin = holder.linearLayout;

        textViewName.setText(dataSet.get(listPosition).getName());
        textViewUid.setText(dataSet.get(listPosition).getUid());
        textViewStatus.setText(dataSet.get(listPosition).getStatus());

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
                if (type == 1) {
                    onClickListener.onClick_d(listPosition);
                }
                if (type1 == 1) {
                    onClick.onClick_d(listPosition);
                }
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
            this.linearLayout = itemView.findViewById(R.id.linear);
            this.textName = itemView.findViewById(R.id.txtName);
            this.textUid = itemView.findViewById(R.id.txtUid);
            this.textStatus = itemView.findViewById(R.id.txtStatus);

        }
    }
}

