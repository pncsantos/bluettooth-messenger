package com.bluetoothmessenger.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

   /*
   * Change image solid color
   *
   * @param ImageView: image to be changed
   * @param resources : resources
   * @param color : set color to changed
   *
   */
    public static void changeImageColor(ImageView img, Resources resources, int color) {
        final int newColor = resources.getColor(color);
        img.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
    }

    /*
    * Format date created to readable format 'dd/mm/yy'
    *
    * @param msgCreated: timestamp
    * @return a new formatted date
    */
    public static String formatDate(long msgCreated) {
        Date date = new Date(msgCreated);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
        return df2.format(date);
    }

    /* convert pixel to dp */
    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
