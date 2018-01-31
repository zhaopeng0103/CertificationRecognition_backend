package com.peng.certrecognition.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String getTransformDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public static String getTransformDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static void main(String[] args) {
        System.out.println(getTransformDate(new Date()));
    }

}
