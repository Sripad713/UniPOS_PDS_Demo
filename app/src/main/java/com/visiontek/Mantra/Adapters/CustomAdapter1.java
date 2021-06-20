package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Activities.DealerAuthenticationActivity;
import com.visiontek.Mantra.Activities.RationDetailsActivity;
import com.visiontek.Mantra.Activities.ReceiveGoodsActivity;
import com.visiontek.Mantra.Models.DATAModels.DataModel1;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter1 extends RecyclerView.Adapter<CustomAdapter1.MyViewHolder> {

    int type;
    int type1;
    private final List<DataModel1> dataSet;
    private RationDetailsActivity.OnClickListener onClickListener;
    private ReceiveGoodsActivity.OnClickListener onClick;
    private final Context context;


    public CustomAdapter1(Context context, ArrayList<DataModel1> data, RationDetailsActivity.OnClickListener onClickListener, int type) {

        this.dataSet = data;
        this.context = context;
        this.onClickListener = onClickListener;
        this.type = type;
    }

    public CustomAdapter1(Context context, ArrayList<DataModel1> data, ReceiveGoodsActivity.OnClickListener onClick, int type1) {

        this.dataSet = data;
        this.context = context;
        this.onClick = onClick;
        this.type1 = type1;
    }

    /* public CustomAdapter1(Context context, List<DataModel1> data, Ration_details.OnEditTextChanged editTextChanged, View.OnClickListener onEditTextChanged) {


     }*/
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row5, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        final DataModel1 data1 = dataSet.get(position);
        holder.Tot.setText(data1.getTot());
        holder.Bal.setText(data1.getBal());
        holder.Rate.setText(data1.getRate());
        holder.Issue.setText(data1.getReq());
        holder.Close.setText(data1.getClose());
        LinearLayout lin = holder.linearLayout;

        if (data1.isSelected) {
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
                data1.isSelected = true;
                if (type == 1) {
                    onClickListener.onClick_d(position);
                }
                if (type1 == 1) {
                    onClick.onClick_d(position);
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
