package com.peng.certrecognition.util;

import java.util.UUID;

public class UUIDUtils {

    public static String genUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void main(String[] args) {
        System.out.println(genUUID());
    }

}
