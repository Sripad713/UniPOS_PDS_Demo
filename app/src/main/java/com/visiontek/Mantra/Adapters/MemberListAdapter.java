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
import com.visiontek.Mantra.Models.DATAModels.MemberListModel;
import com.visiontek.Mantra.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.MyViewHolder> {


    Context context;
    private final ArrayList<MemberListModel> dataSet;
    private final MemberDetailsActivity.OnClickMember onClickMember;

    public MemberListAdapter(Context context, ArrayList<MemberListModel> data,
                             MemberDetailsActivity.OnClickMember onClickMember) {
        this.context = context;
        this.dataSet = data;
        this.onClickMember = onClickMember;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item2_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final MemberListModel model = dataSet.get(listPosition);

        LinearLayout lin = holder.linearLayout;
        TextView textViewName = holder.textName;
        TextView textViewUid = holder.textUid;

        textViewName.setText(dataSet.get(listPosition).getName());
        textViewUid.setText(dataSet.get(listPosition).getUid());

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

                onClickMember.onClick(listPosition);

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
        LinearLayout linearLayout;

        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.LINEAR);
            this.textName = itemView.findViewById(R.id.A);
            this.textUid = itemView.findViewById(R.id.B);

        }
    }
}

