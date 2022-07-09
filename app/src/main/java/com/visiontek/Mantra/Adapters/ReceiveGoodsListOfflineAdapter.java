package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.offlineRecvNew;
import com.visiontek.Mantra.Models.DATAModels.ReceiveGoodsOfflineListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ReceiveGoodsListOfflineAdapter extends RecyclerView.Adapter<ReceiveGoodsListOfflineAdapter.MyViewHolder> {


    Context context;
    private final ArrayList<ReceiveGoodsOfflineListModel> dataSet;
    private final offlineRecvNew.OnClickReceived OnClickReceived;

    public ReceiveGoodsListOfflineAdapter(Context context, ArrayList<ReceiveGoodsOfflineListModel> data,
                                          offlineRecvNew.OnClickReceived OnClickReceived) {
        this.context = context;
        this.dataSet = data;
        this.OnClickReceived = OnClickReceived;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recvgoods, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final ReceiveGoodsOfflineListModel model = dataSet.get(listPosition);

        LinearLayout lin = holder.linearLayout;
        TextView textViewComm = holder.textComm;
        TextView textViewScheme = holder.textScheme;
        TextView textViewAllot = holder.textAllot;
        TextView textViewDispatch = holder.textDispatch;

        textViewComm.setText(dataSet.get(listPosition).getComm());
        textViewScheme.setText(dataSet.get(listPosition).getScheme());
        textViewAllot.setText(dataSet.get(listPosition).getReceived());
        textViewDispatch.setText(dataSet.get(listPosition).getUnits());



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

                OnClickReceived.onClick(listPosition);

                notifyDataSetChanged();
            }
        });

        /*holder.textComm.setOnClickListener(new View.OnClickListener() {
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
*/
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


        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textComm = itemView.findViewById(R.id.A);
            this.textScheme = itemView.findViewById(R.id.B);
            this.textAllot = itemView.findViewById(R.id.C);
            this.textDispatch = itemView.findViewById(R.id.D);


        }
    }
}