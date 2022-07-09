package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class MyCustomTextView_Bold extends AppCompatTextView {

	public MyCustomTextView_Bold(Context context, AttributeSet attr){
		super(context,attr);
		this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Sansation-Bold.ttf"));
	}
}