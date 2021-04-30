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
     * �ж��ǲ���ɫ�ʶ�С�ڵ���100
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
     * �ж��ǲ���ɫ�ʶȴ���100
     *
     * @param colorInt
     * @return
     */
    public static int isWhite(int colorInt) {
        Color color = new Color(colorInt);
//        �Ѹ����ص�� ��ɫֵ  ��� �������100  �򷵻� 1 ���� ����0
        if (color.getRed() + color.getGreen() + color.getBlue() > 100) {
            return 1;
        }
        return 0;
    }

    /**
     * ����ͼƬ��ÿ�����ص�
     * ɾ����ɫ
     *
     * @param picFile
     * @return
     * @throws Exception
     */
    public static BufferedImage removeBackgroud(String picFile) throws Exception {
        BufferedImage img = ImageIO.read(new File(picFile));
//        ��ȡͼƬ�� ���(���ص�)
        int width = img.getWidth();
//        ��ȡͼƬ�� �߶�(���ص�)
        int height = img.getHeight();
//        ������
        for (int x = 0; x < width; ++x) {
//            ������
            for (int y = 0; y < height; ++y) {
//                ��ȡ���ص�� ɫ����ֵ  �������100 ��᷵��1
                if (isWhite(img.getRGB(x, y)) == 1) {
//                    �������ص�����Ϊ��ɫ��
                    img.setRGB(x, y, Color.WHITE.getRGB());
                } else {
//                    �������ص�����Ϊ��ɫ��
                    img.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return img;
    }

    /**
     * ͼƬ��ȡ
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
     * ��ȡtrain ��������ͼ
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
     * ���ָ�õ�ÿһС���������Ա� �����ؽ��
     *  ���ָ�ŵ�һС���������������жԱ�  ����ֵ���ٵ��Ǹ����Ǹ�С��ͼƬ�� ��ֵ
     * @param img
     * @param map
     * @return
     */
    public static String getSingleCharOcr(BufferedImage img,
                                          Map<BufferedImage, String> map) {
        String result = "";

        int width = img.getWidth();
        int height = img.getHeight();
//        ͼƬ���
        int min = width * height;
//      ����������ƥ���ͼƬ
        for (BufferedImage bi : map.keySet()) {
//            ���� start
           String s =  map.get(bi);
//           ����  end
            int count = 0;
//           ����ѭ�� ��������ָ��ѭ��
            Label1:
//           ѭ������ ��׼ͼƬ�ʹ�ƥ��ͼƬ��  ���ص�
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
//                    ��ȡͬһ��λ�õ�����ͼƬ�� ���ص����ɫ��ɫֵ ��� (��ɫֵ��Ӵ���100 ����1) ����ͬһ��λ�õ���ɫֵ����ͬ
                    int isWhiteimg = isWhite(img.getRGB(x, y));
                    int isWhitebi = isWhite(bi.getRGB(x, y));
                    if (isWhiteimg != isWhitebi) {
//                       �����������ֵ �����  ��count++
                        count++;
                        if (count >= min)//������ص㰤������� count ���������
                            break Label1;
                    }
                }
            }
//            ��� ����ȵ� С���� ͼƬ���
            if (count < min) {
//                ���ж��ǲ��Ǹ���ֵ����ֵ  Ҳ����min  ����Ϊ ��εĲ���ȵ����ص���
//                ����ƥ���ͼƬ ȫ�����׼����Ƭ  ƥ��һ��
//                ��ô����ȵ����ص� ���ٵ��Ǹ� ���Ƕ�Ӧ������
                min = count;
                result = map.get(bi);
            }
        }
        return result;
    }

    /**
     * ��ϴ���ͼƬ����һ���ļ�����
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static String getAllOcr(String file) throws Exception {
        //��ϴͼƬ  ɾ����ɫ
        BufferedImage img = removeBackgroud(file);
//		System.out.println("img:"+img);
        //��ȡͼƬ
        List<BufferedImage> listImg = splitImage(img);
        //��ȡ���ڶԱȵ�ͼƬ��ֵ�Լ���  ͼƬ������
        Map<BufferedImage, String> map = loadTrainData();

        String result = "";
//        ����������ȡ����ͼƬ
        for (BufferedImage bi : listImg) {
            result += getSingleCharOcr(bi, map);
        }
        ImageIO.write(img, "JPG", new File("result\\" + result + ".jpg"));
        return result;
    }

    /**
     * ����ͼƬ
     */
    public static void downloadImage() {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(
                "http://www.puke888.com/authimg.php");
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(500);
                // ִ��getMethod
                int statusCode = httpClient.executeMethod(getMethod);
                if (statusCode != HttpStatus.SC_OK) {
                    System.err.println("Method failed: "
                            + getMethod.getStatusLine());
                }
                // ��ȡ����
                String picName = "img\\" + i + ".jpg";
                InputStream inputStream = getMethod.getResponseBodyAsStream();
                OutputStream outStream = new FileOutputStream(picName);
                IOUtils.copy(inputStream, outStream);
                outStream.close();
                System.out.println(i + "OK!");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // �ͷ�����
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
