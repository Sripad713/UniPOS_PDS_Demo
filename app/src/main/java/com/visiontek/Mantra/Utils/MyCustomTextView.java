package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class MyCustomTextView extends AppCompatTextView {
	
	public MyCustomTextView(Context context, AttributeSet attr){
		super(context,attr);
		this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Sansation-Regular.ttf"));
	}
	public MyCustomTextView(Context context){
		super(context);
		this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Sansation-Regular.ttf"));
	}
	public MyCustomTextView(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
		this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Sansation-Regular.ttf"));
	}
}