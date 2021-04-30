package cn.pwntcha.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class utli_crop {
    boolean cropImage(InputStream inputStream, int x, int y, int w, int h, String sufix, File file)throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        bufferedImage = bufferedImage.getSubimage(x,y,w,h);
        return ImageIO.write(bufferedImage,sufix,file);
    }
}
