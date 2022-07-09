package com.visiontek.Mantra.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;

public class LogoUtil {

    public static byte[] toBytes(String logo) throws Exception {

        System.out.println("In toBytes");

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Bitmap image = BitmapFactory.decodeFile(logo);

        int w = 384;
        int h = image.getHeight();

        BitOutputStream bos = new BitOutputStream(os);

        System.out.println("Before For loop");
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                int color = image.getPixel(x, y);
                int a = (int) (0.299 * Color.red(color) + 0.587
                        * Color.green(color) + 0.114 * Color.blue(color));
                //if (a < 128) { // IF CONDITION LESSTHAN 128 THEN IMAGE WILL PRINT REVERSE COLOR
                // IF CONDITION GREATERTHAN 128 THEN IMAGE WILL PRINT IN ORIGINAL COLOR

                if (a > 128) {
                    bos.write(0);
                } else {
                    bos.write(1);
                }
            }
        }

        System.out.println("After For loop");
        return os.toByteArray();
    }

}
