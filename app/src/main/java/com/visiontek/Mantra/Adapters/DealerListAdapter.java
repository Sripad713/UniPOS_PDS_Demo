package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.DealerAuthenticationActivity;
import com.visiontek.Mantra.Activities.DealerDetailsActivity;
import com.visiontek.Mantra.Models.DATAModels.DealerListModel;
import com.visiontek.Mantra.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class DealerListAdapter extends RecyclerView.Adapter<DealerListAdapter.MyViewHolder> {


    Context context;
    int type;
    private final ArrayList<DealerListModel> dataSet;
    private  DealerDetailsActivity.OnClickDealer OnClickDealer;
    private  DealerAuthenticationActivity.OnClickDealerAUTH OnClickDealerAUTH;

    public DealerListAdapter(Context context, ArrayList<DealerListModel> data,
                             DealerDetailsActivity.OnClickDealer OnClickDealer, int type) {
        this.dataSet = data;
        this.context = context;
        this.type = type;
        this.OnClickDealer = OnClickDealer;
    }
    public DealerListAdapter(Context context, ArrayList<DealerListModel> data,
                             DealerAuthenticationActivity.OnClickDealerAUTH OnClickDealerAUTH, int type) {
        this.dataSet = data;
        this.context = context;
        this.type = type;
        this.OnClickDealerAUTH = OnClickDealerAUTH;

    }
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item3_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final DealerListModel model = dataSet.get(listPosition);

        LinearLayout lin = holder.linearLayout;
        TextView textViewName = holder.textName;
        TextView textViewStatus = holder.textType;
        TextView textViewUid = holder.textUid;

        textViewName.setText(dataSet.get(listPosition).getName());
        textViewStatus.setText(dataSet.get(listPosition).getType());
        textViewUid.setText(dataSet.get(listPosition).getUid());

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

                if (type==1) {
                    OnClickDealerAUTH.onClick(listPosition);
                }else {
                    OnClickDealer.onClick(listPosition);
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
        TextView textType;
        TextView textUid;
        LinearLayout linearLayout;
        //CardView cardView ;

        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textName = itemView.findViewById(R.id.A);
            this.textType = itemView.findViewById(R.id.B);
            this.textUid = itemView.findViewById(R.id.C);

        }
    }
}

