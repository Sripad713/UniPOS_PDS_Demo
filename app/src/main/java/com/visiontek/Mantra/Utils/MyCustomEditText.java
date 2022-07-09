package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by TS-Android on 12/14/2017.
 */

public class MyCustomEditText  extends AppCompatEditText {

    public MyCustomEditText(Context context, AttributeSet attr){
        super(context,attr);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Sansation-Bold.ttf"));
    }
}