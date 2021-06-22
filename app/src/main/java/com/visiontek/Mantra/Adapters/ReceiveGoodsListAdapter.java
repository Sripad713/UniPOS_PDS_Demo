package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.RationDetailsActivity;
import com.visiontek.Mantra.Activities.ReceiveGoodsActivity;
import com.visiontek.Mantra.Models.DATAModels.RationListModel;
import com.visiontek.Mantra.Models.DATAModels.ReceiveGoodsListModel;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsDetails;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ReceiveGoodsListAdapter extends RecyclerView.Adapter<ReceiveGoodsListAdapter.MyViewHolder> {


    Context context;
    private final ArrayList<ReceiveGoodsListModel> dataSet;
    private final ReceiveGoodsActivity.OnClickReceived OnClickReceived;

    public ReceiveGoodsListAdapter(Context context, ArrayList<ReceiveGoodsListModel> data,
                                   ReceiveGoodsActivity.OnClickReceived OnClickReceived) {
        this.context = context;
        this.dataSet = data;
        this.OnClickReceived = OnClickReceived;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row6, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final ReceiveGoodsListModel model = dataSet.get(listPosition);

        LinearLayout lin = holder.linearLayout;
        TextView textViewComm = holder.textComm;
        TextView textViewScheme = holder.textScheme;
        TextView textViewAllot = holder.textAllot;
        TextView textViewDispatch = holder.textDispatch;
        TextView textViewReceived = holder.textReceived;


        textViewComm.setText(dataSet.get(listPosition).getComm());
        textViewScheme.setText(dataSet.get(listPosition).getScheme());
        textViewAllot.setText(dataSet.get(listPosition).getAllot());
        textViewDispatch.setText(dataSet.get(listPosition).getDispatch());
        textViewReceived.setText(dataSet.get(listPosition).getReceived());


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

                OnClickReceived.onClick(listPosition);

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
        TextView textScheme;
        TextView textAllot;
        TextView textDispatch;
        TextView textReceived;

        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textComm = itemView.findViewById(R.id.A);
            this.textScheme = itemView.findViewById(R.id.B);
            this.textAllot = itemView.findViewById(R.id.C);
            this.textDispatch = itemView.findViewById(R.id.D);
            this.textReceived = itemView.findViewById(R.id.E);

        }
    }
}

