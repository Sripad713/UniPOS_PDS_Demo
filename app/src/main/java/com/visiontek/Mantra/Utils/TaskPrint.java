package com.visiontek.Mantra.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mantra.mTerminal100.MTerminal100API;

import java.io.IOException;
import java.io.InputStream;

import static com.mantra.mTerminal100.printer.PrinterAPI.FontStyleUnderline2DotThick;
import static com.mantra.mTerminal100.printer.PrinterAPI.FontTypeStandardASCII;

public class TaskPrint implements Runnable {

    MTerminal100API mTerminal100API;
    String[] text;
    Activity activity;
    Context context;
    int type;


    public TaskPrint(MTerminal100API mTerminal100API, String[] text,
                     Activity activity, Context context,int type) {
        this.mTerminal100API = mTerminal100API;
        this.text = text;
        this.activity = activity;
        this.context= context;
        this.type=type;
    }

    @Override
    public void run() {
        int bPrintResult = 0;
        bPrintResult = PrintTicket(type);

        final boolean bIsOpened = mTerminal100API.getIOIsOpened();
        final int finalBPrintResult = bPrintResult;

       /* activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), (finalBPrintResult == 0 || finalBPrintResult == 1) ? context.getResources().getString(R.string.printsuccess) : context.getResources().getString(R.string.printfailed) + " " + Prints.ResultCodeToString(finalBPrintResult), Toast.LENGTH_SHORT).show();
                if (finalBPrintResult == 0) {
                    //MediaPlayer.create(activity,R.raw.c100073).start();
                }
            }
        });*/
    }

    private int PrintTicket(int type) {
        if (type==0) {
            if (!text[0].equals("0")) {
                System.out.println("**************************0");
                Bitmap bmYellowmen;
                bmYellowmen = getImageFromAssetsFile(context);
                int p = printBmp(300, bmYellowmen, 1);
                if (p != 0) return p;
            }
            if (!text[1].equals("0")) {
                System.out.println("**************************1" + text[1]);
                int p = print(0, 0, text[1], 1);
                if (p != 0) return p;
            }
            if (!text[2].equals("0")) {
                System.out.println("**************************2" + text[2]);
                int p = print(0, 0, text[2], 0);
                if (p != 0) return p;
            }
            if (!text[3].equals("0")) {
                System.out.println("**************************3" + text[3]);
                return print(0, 0, text[3], 1);
            }
        }else {
            Bitmap bmYellowmen;
            if (!text[0].equals("0")) {
                System.out.println("**************************0");
                bmYellowmen = getImageFromAssetsFile(context);
                int p = printBmp(300,bmYellowmen,1);
                if (p != 0) return p;
            }
            if (!text[1].equals("0")) {
                System.out.println("**************************1");
                bmYellowmen = Util.getImageFromAssetsFile(context,"header.bmp");
                int p = printBmp(500,bmYellowmen,1);
                if (p != 0) return p;
            }
            if (!text[2].equals("0")) {
                System.out.println("**************************2");
                bmYellowmen = Util.getImageFromAssetsFile(context, "body.bmp");
                int p = printBmp(500,bmYellowmen,0);
                if (p != 0) return p;
            }
            if (!text[3].equals("0")) {
                System.out.println("**************************3");
                bmYellowmen = Util.getImageFromAssetsFile(context,"tail.bmp");
                int p = printBmp(500,bmYellowmen,1);
                if (p != 0) return p;
            }
        }
        return -1;
    }

    private int print(int i, int i1, String s, int i2) {
        mTerminal100API.setAlign(i2);
        return mTerminal100API.printString(i, i1, s, 0, FontStyleUnderline2DotThick, FontTypeStandardASCII);
    }

    private int printBmp(int width, Bitmap bmYellowmen, int align) {
        mTerminal100API.setAlign(align);
        return mTerminal100API.PrintImageBitmap(width, 0, bmYellowmen, 0);
    }

    private static Bitmap getImageFromAssetsFile(Context ctx) {
        Bitmap image = null;
        AssetManager am = ctx.getResources().getAssets();
        try {
            InputStream is = am.open("clogo.png");
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

}
