package cn.pwntcha;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImagePreProcess6 {

    public static int isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
            return 1;
        }
        return 0;
    }

    public static int isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() > 100) {
            return 1;
        }
        return 0;
    }

    public static BufferedImage removeBackgroud(String picFile)
            throws Exception {
        BufferedImage img = ImageIO.read(new File(picFile));
        int width = img.getWidth();
        int height = img.getHeight();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (isWhite(img.getRGB(x, y)) == 1) {
                    img.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    img.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return img;
    }

    public static java.util.List<BufferedImage> splitImage(BufferedImage img)
            throws Exception {
        java.util.List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
        subImgs.add(img.getSubimage(6, 6, 8, 10));
        subImgs.add(img.getSubimage(22, 8, 8, 10));
        subImgs.add(img.getSubimage(38, 5, 8, 10));
        subImgs.add(img.getSubimage(46, 7, 8, 10));
        return subImgs;
    }

    public static Map<BufferedImage, String> loadTrainData() throws Exception {
        Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
        File dir = new File("train6");
        File[] files = dir.listFiles();
        for (File file : files) {
            map.put(ImageIO.read(file), file.getName().charAt(0) + "");
        }
        return map;
    }

    public static String getSingleCharOcr(BufferedImage img,
                                          Map<BufferedImage, String> map) {
        String result = "";
        int width = img.getWidth();
        int height = img.getHeight();
        int min = width * height;
        for (BufferedImage bi : map.keySet()) {
            int count = 0;
            Label1: for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    if (isWhite(img.getRGB(x, y)) != isWhite(bi.getRGB(x, y))) {
                        count++;
                        if (count >= min)
                            break Label1;
                    }
                }
            }
            if (count < min) {
                min = count;
                result = map.get(bi);
            }
        }
        return result;
    }

    public static String getAllOcr(String file) throws Exception {
        BufferedImage img = removeBackgroud(file);
        List<BufferedImage> listImg = splitImage(img);
        Map<BufferedImage, String> map = loadTrainData();
        String result = "";
        for (BufferedImage bi : listImg) {
            result += getSingleCharOcr(bi, map);
        }
        ImageIO.write(img, "JPG", new File("result6\\"+result+".jpg"));
        return result;
    }

    public static void downloadImage() {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(
                "http://game.tom.com/checkcode.php");
        for (int i = 0; i < 1; i++) {
            try {
                Thread.sleep(500);
                // ??????getMethod
                int statusCode = httpClient.executeMethod(getMethod);
                if (statusCode != HttpStatus.SC_OK) {
                    System.err.println("Method failed: "
                            + getMethod.getStatusLine());
                }
                // ????????????
                String picName = "img6\\" + i + ".jpg";
                InputStream inputStream = getMethod.getResponseBodyAsStream();
                OutputStream outStream = new FileOutputStream(picName);
                IOUtils.copy(inputStream, outStream);
                outStream.close();
                System.out.println(i+"OK!");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // ????????????
                getMethod.releaseConnection();
            }
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//        downloadImage();
		for (int i = 0; i < 30; ++i) {
			String text = getAllOcr("img6\\" + i + ".jpg");
			System.out.println(i + ".jpg = " + text);
		}
    }
}
