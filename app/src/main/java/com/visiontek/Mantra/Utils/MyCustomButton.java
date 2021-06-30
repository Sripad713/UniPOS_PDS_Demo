package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by TS-Android on 12/14/2017.
 */

public class MyCustomButton extends AppCompatButton {

    public MyCustomButton(Context context, AttributeSet attr){
        super(context,attr);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Sansation-Bold.ttf"));
    }
}