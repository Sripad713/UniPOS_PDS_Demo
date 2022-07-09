package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.RationNumbersAdapter;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.Member;
import com.visiontek.Mantra.R;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class OfflineRcNumbers extends BaseActivity{


    //RecyclerView.Adapter adapter;
    RationNumbersAdapter adapter;
    RecyclerView recyclerView;
    List<String> arraydata;
    Context context;
    DatabaseHelper databaseHelper;
    EditText editText;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void saveInfo(Context context) {
        try {
            editText = findViewById(R.id.id);
            recyclerView = findViewById(R.id.my_recycler_view);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            arraydata = new ArrayList<>();
            memberConstants = new Member();
            arraydata = databaseHelper.getRcNumber();
            adapter = new RationNumbersAdapter(context, arraydata);
            recyclerView.setAdapter(adapter);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //after the change calling the method and passing the search input
                    filter(editable.toString());
                }
            });

        }catch (Exception e){

            e.printStackTrace();
        }
    }


    private void show_AlertDialog(String headermsg,String bodymsg, String talemsg, int i) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.alertdialog);
        Button confirm = (Button) dialog.findViewById(R.id.alertdialogok);
        TextView head = (TextView) dialog.findViewById(R.id.alertdialoghead);
        TextView body = (TextView) dialog.findViewById(R.id.alertdialogbody);
        TextView tale = (TextView) dialog.findViewById(R.id.alertdialogtale);
        //TextView title = (TextView) dialog.findViewById(R.id.alertdialogTitle);
        head.setText(headermsg);
        body.setText(bodymsg);
        tale.setText(talemsg);
        //title.setText(titlemsg);
        confirm.setOnClickListener(v -> {
            preventTwoClick(v);
            dialog.dismiss();
           /* if(i == 1){

                editText.setText(" ");
            }*/

        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    private void filter(String text) {
        //new array list that will hold the filtered data
        ArrayList<String> filterdNames = new ArrayList<>();

        //looping through existing elements
        for (String s : arraydata) {
            //if the existing elements contains the search input
            if (s.toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filterdNames.add(s);

            }
        }
        if(filterdNames.isEmpty()){

            //Toast.makeText(context, "Not Found", Toast.LENGTH_SHORT).show();
            //show_AlertDialog("RC Member","Not Found","",0);
            show_AlertDialog(context.getResources().getString(R.string.Member_Details)+text,context.getResources().getString(R.string.Card_Number_does_not_exist),"",0);
            editText.setText("");

        }

        //calling a method of the adapter class and passing the filtered list
        adapter.filterList(filterdNames);

    }





    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initialize() {


        try {
            context = OfflineRcNumbers.this;
            databaseHelper = new DatabaseHelper(context);
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_offline_rcnumbers, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();
            saveInfo(context);
            Button back=findViewById(R.id.back);

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    finish();
                }
            });
        }catch (Exception ex){

            ex.printStackTrace();
            //Timber.tag("DeviceInfo-onCreate-").e(ex.getMessage(),"");
        }



    }

    @Override
    public void initializeControls() {

        toolbarActivity.setText(context.getResources().getString(R.string.Rc_Numbers));
    }
}
