package com.visiontek.Mantra.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.visiontek.Mantra.R;

import java.util.ArrayList;
import java.util.List;

import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class FusionFingerSectionActivity extends AppCompatActivity {
    List<String> fingersSelectedList = new ArrayList<>();
    Button scanfp,back;
    Context context;
    String checkfingeritems[] = {"LEFT_THUMB,LEFT_INDEX,LEFT_MIDDLE,LEFT_RING,LEFT_LITTLE,RIGHT_THUMB,RIGHT_INDEX,RIGHT_MIDDLE,RIGHT_RING,RIGHT_LITTLE"};
    String histr;
    static String L;
    String LEFT_THUMB;
    String strposh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fusion_finger_select);
        context = FusionFingerSectionActivity.this;
        back =findViewById(R.id.btn_back);
        scanfp =findViewById(R.id.scanFP);
        fingersSelectedList = new ArrayList<>();

        back.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {

                  Intent intent = new Intent(context, DealerDetailsActivity.class);
                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                  startActivity(intent);

              }
          });

        scanfp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if(fingersSelectedList.size()!=0){
                  if (fingersSelectedList.size()<2||fingersSelectedList.size()>=3) {
                          System.out.println("Please select Any two fingres=====CASE 1");
                          show_AlertDialog(context.getResources().getString(R.string.Dealer_Authentication),context.getResources().getString(R.string.Please_Select_Any_Two_Fingers),"",0);
                   }else if(fingersSelectedList.size()==2){

                                   String str = fingersSelectedList.get(0) + "," + fingersSelectedList.get(1);
                                   Intent i = new Intent();
                                   i.putExtra("FUSION_DATA", str);
                                   setResult(RESULT_OK, i);
                                   finish();
                                   System.out.println("Scan FPP");
                  }
              }else{
                  System.out.println("Please Select Any Two Fingers ===3");
                  show_AlertDialog(context.getResources().getString(R.string.Dealer_Authentication),context.getResources().getString(R.string.Please_Select_Any_Two_Fingers),"",0);
              }

            }
        });
    }
    public  void onRadioButtonClicked(View view){
        System.out.println("Onclicked====");
        CheckBox checkBox = (CheckBox) view;
        if(checkBox != null && checkBox instanceof  CheckBox){
               String text = checkBox.getText().toString();
                if(checkBox.isChecked()) {
                    fingersSelectedList.add(text);
                    System.out.println("PRINT ===="+fingersSelectedList);
                }
                else {
                    fingersSelectedList.remove(text);
               }
        }

    }
    private void show_AlertDialog(String headermsg, String bodymsg, String talemsg, int i) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.alertdialog);
        Button confirm = (Button) dialog.findViewById(R.id.alertdialogok);
        TextView head = (TextView) dialog.findViewById(R.id.alertdialoghead);
        TextView body = (TextView) dialog.findViewById(R.id.alertdialogbody);
        TextView tale = (TextView) dialog.findViewById(R.id.alertdialogtale);
        head.setText(headermsg);
        body.setText(bodymsg);
        tale.setText(talemsg);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                dialog.dismiss();
                /*if (i == 1) {
                    Intent intent = new Intent(context, DealerDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }else if(i==2)
                {
                    Intent intent = new Intent(context, DealerDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }*/

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }



}