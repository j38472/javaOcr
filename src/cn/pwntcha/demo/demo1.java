package cn.pwntcha.demo;

import java.awt.*;
import java.awt.image.BufferedImage;

public class demo1 {
    public static int isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
            return 1;
        }
        return 0;
    }

    /**
     *
     * @param img
     * @param w 图片宽
     * @param h 图片高
     */
    public static void imgRgb(BufferedImage img,int w,int h){
        for (int i = 0; i <w ; i++) {
            for (int j = 0; j < h; j++) {
                int rgb = img.getRGB(i,j);
            }
        }

    }

    public static void main(String[] args) {


//        for (int i = 0; i < 255; i++) {
//            System.out.println(i+":"+isBlack(i));
//        }
    }

}
