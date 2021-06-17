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
        System.out.println("Custommm");

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row4, parent, false);

        //view.setOnClickListener(Dealer_Details.myOnClickListener);

        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int listPosition) {

        final DataModel2 model = dataSet.get(listPosition);

        TextView textViewName = MyViewHolder.textViewName;
        TextView textViewVersion = MyViewHolder.textBalance;
        TextView editTextView = MyViewHolder.textObservation;
        TextView textViewVariation = MyViewHolder.textVariation;

        System.out.println(dataSet);
        textViewName.setText(dataSet.get(listPosition).getTot());
        textViewVersion.setText(dataSet.get(listPosition).getBal());
        editTextView.setText(dataSet.get(listPosition).getReq());
        textViewVariation.setText(dataSet.get(listPosition).getRate());

        LinearLayout lin = holder.linearLayout;

        if (model.isSelected) {
            lin.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));

        } else {
            lin.setBackgroundColor(context.getResources().getColor(R.color.background));

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
        return (dataSet == null) ? 0 : dataSet.size();
    }

    /* public CustomAdapter1(Context context, ArrayList<DataModel1> data, Inspection.OnEditTextChanged onEditTextChanged) {
     }
 */
    static class MyViewHolder extends RecyclerView.ViewHolder {

        static TextView textViewName;
        static TextView textBalance;
        static TextView textObservation;
        static TextView textVariation;
        LinearLayout linearLayout;


        MyViewHolder(View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.linear);
            textViewName = itemView.findViewById(R.id.total);
            textBalance = itemView.findViewById(R.id.bal);
            textObservation = itemView.findViewById(R.id.rate);
            textVariation = itemView.findViewById(R.id.close);

        }
    }
}


