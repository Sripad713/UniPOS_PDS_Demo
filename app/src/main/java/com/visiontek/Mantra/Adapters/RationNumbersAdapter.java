package com.visiontek.Mantra.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.R;

import java.util.List;

public class RationNumbersAdapter  extends RecyclerView.Adapter<RationNumbersAdapter.MyViewHolder> {


        Context context;
       public List<String> dataSet;


public RationNumbersAdapter(Context context, List<String> data) {
        this.context = context;
        this.dataSet = data;
        }


@Override
public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item1_row, parent, false);
        return new MyViewHolder(view);
        }

@Override
public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewName = holder.textName;
        //TextView textViewUid = holder.textUid;
        textViewName.setText(dataSet.get(listPosition));
        //holder.textName.setTextColor(Color.parseColor("#ffffff"));
        //textViewUid.setText(dataSet.get(listPosition).getUid());
        }

@Override
public int getItemCount() {
        return dataSet.size();
        }

public void filterList(List<String> filterdNames) {
        this.dataSet = filterdNames;
        notifyDataSetChanged();
        }

static class MyViewHolder extends RecyclerView.ViewHolder {

    TextView textName;
    TextView textUid;
    LinearLayout linearLayout;

    MyViewHolder(View itemView) {
        super(itemView);
        this.linearLayout = itemView.findViewById(R.id.LINEAR);
        this.textName = itemView.findViewById(R.id.A);
        //this.textUid = itemView.findViewById(R.id.B);

    }
}

}
