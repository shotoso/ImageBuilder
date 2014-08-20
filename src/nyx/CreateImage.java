package nyx;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

public class CreateImage {

  static int START = 0;
  static int COUNT = 5;
  static int GPS_CUTOFF = 2;
  static double longitude;
  static double latitude;

  private static void generateGps() {
    Random random = new Random();
    latitude = -180d + (180d - (-180d)) * random.nextDouble();
    longitude = -180d + (180d - (-180d)) * random.nextDouble();
  }

  public static void main(String[] args) {
    createFolder();
    for (int i = START; i < COUNT; i++) {
      if (i % GPS_CUTOFF == 0) {
        generateGps();
      }
      createImage(i);
    }
  }

  public static void createFolder() {
    File file = new File("images/");
    file.mkdir();
  }

  public static void createImage(int count) {
    System.out.println("created " + count);
    try {
      Random random = new Random();
      BufferedImage img = new BufferedImage(2448, 3264, BufferedImage.TYPE_INT_RGB);
      int colorSize = random.nextInt(50) + 1;
      int colorRows = random.nextInt(1000) + 100;
      int[] rgb = new int[colorSize];

      for (int i = 0; i < colorSize; i++) {
        int a = random.nextInt(256), b = random.nextInt(256), c = random.nextInt(256);
        int color = new Color(a, b, c).getRGB();
        rgb[i] = color;
      }
      int colorCount = 0;
      int color = 0;
      int type = 2;

      int width = img.getWidth(), height = img.getHeight();;
      if (type == 0) {
        for (int i = 0; i < width; i++) {
          for (int j = 0; j < height; j++) {
            if (colorCount == 0) {
              color = rgb[random.nextInt(colorSize)];
            }
            img.setRGB(i, j, color);
            colorCount++;
            if (colorCount == colorRows) {
              colorCount = 0;
            }
          }
        }
      } else if (type == 1) {
        for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
            if (colorCount == 0) {
              color = rgb[random.nextInt(colorSize)];
            }
            img.setRGB(j, i, color);
            colorCount++;
            if (colorCount == colorRows) {
              colorCount = 0;
            }
          }
        }
      } else if (type == 2) {
        for (int iBlockCount = 0; iBlockCount <= 12; iBlockCount++) {
          for (int jBlockCount = 0; jBlockCount <= 16; jBlockCount++) {
            if (colorCount == 0) {
              color = rgb[random.nextInt(colorSize)];
            }
            int iS = iBlockCount * 204, iE = Math.min((iBlockCount + 1) * 204, 2448);
            int jS = jBlockCount * 204, jE = Math.min((jBlockCount + 1) * 204, 3264);
            for (int i = iS; i < iE; i++) {
              for (int j = jS; j < jE; j++) {
                img.setRGB(i, j, color);
              }
            }
          }
        }
      }

      File file = new File("images/tmp.jpg");
      ImageIO.write(img, "jpg", file);
      File file2 = new File("images/createdImage" + count + ".jpg");
      writeExif(file, file2);
      file.delete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void writeExif(File jpegImageFile, File dst) throws IOException,
      ImageReadException, ImageWriteException {
    OutputStream os = null;
    try {
      TiffOutputSet outputSet = null;
      IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
      JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
      if (null != jpegMetadata) {
        TiffImageMetadata exif = jpegMetadata.getExif();
        if (null != exif) {
          outputSet = exif.getOutputSet();
        }
      }
      if (null == outputSet) outputSet = new TiffOutputSet();
      {
        outputSet.setGPSInDegrees(longitude, latitude);
      }

      os = new FileOutputStream(dst);
      os = new BufferedOutputStream(os);
      new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
      os.close();
      os = null;
    } finally {
      if (os != null) try {
        os.close();
      } catch (IOException e) {}
    }
  }
}
