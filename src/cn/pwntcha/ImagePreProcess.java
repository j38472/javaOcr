package cn.pwntcha;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

public class ImagePreProcess {
    /**
     * 判断是不是色彩度小于等于100
     *
     * @param colorInt
     * @return
     */
    public static int isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
            return 1;
        }
        return 0;
    }

    /**
     * 判断是不是色彩度大于100
     *
     * @param colorInt
     * @return
     */
    public static int isWhite(int colorInt) {
        Color color = new Color(colorInt);
//        把该像素点的 三色值  相加 如果大于100  则返回 1 否则 返回0
        if (color.getRed() + color.getGreen() + color.getBlue() > 100) {
            return 1;
        }
        return 0;
    }

    /**
     * 遍历图片的每个像素点
     * 删除杂色
     *
     * @param picFile
     * @return
     * @throws Exception
     */
    public static BufferedImage removeBackgroud(String picFile) throws Exception {
        BufferedImage img = ImageIO.read(new File(picFile));
//        获取图片的 宽度(像素点)
        int width = img.getWidth();
//        获取图片的 高度(像素点)
        int height = img.getHeight();
//        遍历高
        for (int x = 0; x < width; ++x) {
//            遍历宽
            for (int y = 0; y < height; ++y) {
//                获取像素点的 色彩数值  如果大于100 这会返回1
                if (isWhite(img.getRGB(x, y)) == 1) {
//                    将该像素点设置为白色的
                    img.setRGB(x, y, Color.WHITE.getRGB());
                } else {
//                    将该像素点设置为黑色的
                    img.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return img;
    }

    /**
     * 图片截取
     *
     * @param img
     * @return
     * @throws Exception
     */
    public static List<BufferedImage> splitImage(BufferedImage img)
            throws Exception {
        List<BufferedImage> subImgs = new ArrayList<BufferedImage>();

        subImgs.add(img.getSubimage(10, 6, 8, 10));
        subImgs.add(img.getSubimage(19, 6, 8, 10));
        subImgs.add(img.getSubimage(28, 6, 8, 10));
        subImgs.add(img.getSubimage(37, 6, 8, 10));
        return subImgs;
    }

    /**
     * 获取train 样本对照图
     *
     * @return
     * @throws Exception
     */
    public static Map<BufferedImage, String> loadTrainData() throws Exception {
        Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
        File dir = new File("train");
        File[] files = dir.listFiles();
        for (File file : files) {
            map.put(ImageIO.read(file), file.getName().charAt(0) + "");
        }
        return map;
    }

    /**
     * 将分割好的每一小块与样本对比 并返回结果
     *  将分割号的一小块与整个样本进行对比  差异值最少的那个就是该小块图片的 数值
     * @param img
     * @param map
     * @return
     */
    public static String getSingleCharOcr(BufferedImage img,
                                          Map<BufferedImage, String> map) {
        String result = "";

        int width = img.getWidth();
        int height = img.getHeight();
//        图片面积
        int min = width * height;
//      遍历整个待匹配的图片
        for (BufferedImage bi : map.keySet()) {
//            测试 start
           String s =  map.get(bi);
//           测试  end
            int count = 0;
//           命名循环 用于跳出指定循环
            Label1:
//           循环遍历 标准图片和待匹配图片的  像素点
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
//                    获取同一个位置的两个图片的 像素点的颜色三色值 如果 (三色值相加大于100 返回1) 两个同一个位置的三色值不相同
                    int isWhiteimg = isWhite(img.getRGB(x, y));
                    int isWhitebi = isWhite(bi.getRGB(x, y));
                    if (isWhiteimg != isWhitebi) {
//                       如果两个像素值 不相等  则count++
                        count++;
                        if (count >= min)//如果像素点挨个跑完后 count 将等于面积
                            break Label1;
                    }
                }
            }
//            如果 不相等的 小于了 图片面积
            if (count < min) {
//                将判断是不是该数值的数值  也就是min  设置为 这次的不相等的像素点数
//                当待匹配的图片 全部与标准的照片  匹配一遍
//                那么不相等的像素点 最少的那个 就是对应的数字
                min = count;
                result = map.get(bi);
            }
        }
        return result;
    }

    /**
     * 清洗后的图片放入一个文件夹中
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static String getAllOcr(String file) throws Exception {
        //清洗图片  删除杂色
        BufferedImage img = removeBackgroud(file);
//		System.out.println("img:"+img);
        //截取图片
        List<BufferedImage> listImg = splitImage(img);
        //获取用于对比的图片键值对集合  图片与名字
        Map<BufferedImage, String> map = loadTrainData();

        String result = "";
//        遍历整个截取出的图片
        for (BufferedImage bi : listImg) {
            result += getSingleCharOcr(bi, map);
        }
        ImageIO.write(img, "JPG", new File("result\\" + result + ".jpg"));
        return result;
    }

    /**
     * 下载图片
     */
    public static void downloadImage() {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(
                "http://www.puke888.com/authimg.php");
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(500);
                // 执行getMethod
                int statusCode = httpClient.executeMethod(getMethod);
                if (statusCode != HttpStatus.SC_OK) {
                    System.err.println("Method failed: "
                            + getMethod.getStatusLine());
                }
                // 读取内容
                String picName = "img\\" + i + ".jpg";
                InputStream inputStream = getMethod.getResponseBodyAsStream();
                OutputStream outStream = new FileOutputStream(picName);
                IOUtils.copy(inputStream, outStream);
                outStream.close();
                System.out.println(i + "OK!");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 释放连接
                getMethod.releaseConnection();
            }
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//		downloadImage();
        for (int i = 0; i < 30; ++i) {
            System.out.println("img\\" + i + ".jpg");
            String text = getAllOcr("img\\" + i + ".jpg");
            System.out.println(i + ".jpg = " + text);
        }
    }
}
