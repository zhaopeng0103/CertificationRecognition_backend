package com.peng.certrecognition.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageUtil {

    public static boolean isImage(File file) {
        boolean flag;
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            if (width == 0 || height == 0) {
                flag = false;
            } else {
                flag = true;
            }
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

}
