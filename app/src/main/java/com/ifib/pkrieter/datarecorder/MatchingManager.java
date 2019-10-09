package com.ifib.pkrieter.datarecorder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.opencv_imgproc.CV_TM_CCOEFF_NORMED;
import static org.bytedeco.javacpp.opencv_imgproc.matchTemplate;


public class MatchingManager {

    private static final String TAG = "MMANAGER";

    public static int areImagesSimilar(Bitmap image1, long eventPhash){
        int dist = hamDist(getPHash(image1), eventPhash);
        return dist;
    }

    public static long getPHash(Bitmap image) {
        float scale_width, scale_height;

        Bitmap bitmap = Bitmap.createScaledBitmap(image, 128, 128, true);

        scale_width = 8.0f / bitmap.getWidth();
        scale_height = 8.0f / bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_width, scale_height);

        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        return getFingerPrint(scaledBitmap);
    }

    private static long getFingerPrint(Bitmap bitmap) {
        double[][] grayPixels = getGrayPixels(bitmap);
        double grayAvg = getGrayAvg(grayPixels);
        return getFingerPrint(grayPixels, grayAvg);
    }

    private static long getFingerPrint(double[][] pixels, double avg) {
        int width = pixels[0].length;
        int height = pixels.length;

        byte[] bytes = new byte[height * width];

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (pixels[i][j] >= avg) {
                    bytes[i * height + j] = 1;
                    stringBuilder.append("1");
                } else {
                    bytes[i * height + j] = 0;
                    stringBuilder.append("0");
                }
            }
        }

        long fingerprint1 = 0;
        long fingerprint2 = 0;
        for (int i = 0; i < 64; i++) {
            if (i < 32) {
                fingerprint1 += (bytes[63 - i] << i);
            } else {
                fingerprint2 += (bytes[63 - i] << (i - 31));
            }
        }

        return (fingerprint2 << 32) + fingerprint1;
    }

    private static double getGrayAvg(double[][] pixels) {
        int width = pixels[0].length;
        int height = pixels.length;
        int count = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                count += pixels[i][j];
            }
        }
        return count / (width * height);
    }


    private static double[][] getGrayPixels(Bitmap bitmap) {
        int width = 8;
        int height = 8;
        double[][] pixels = new double[height][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i][j] = computeGrayValue(bitmap.getPixel(i, j));
            }
        }
        return pixels;
    }

    private static double computeGrayValue(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = (pixel) & 255;
        return 0.3 * red + 0.59 * green + 0.11 * blue;
    }

    private static int hamDist(long finger1, long finger2) {
        int dist = 0;
        long result = finger1 ^ finger2;
        while (result != 0) {
            ++dist;
            result &= result - 1;
        }
        return dist;
    }

    public static Bitmap sliceImage(Bitmap sourceBmp, int left, int top, int right, int bottom){
        Rect rect = new Rect(left, top, right, bottom);
        assert(rect.left < rect.right && rect.top < rect.bottom);
        Bitmap resultBmp = Bitmap.createBitmap(rect.right-rect.left, rect.bottom-rect.top, Bitmap.Config.ARGB_8888);
        new Canvas(resultBmp).drawBitmap(sourceBmp, -rect.left, -rect.top, null);
        return resultBmp;
    }

    public static boolean isInImage(opencv_core.Mat searchIn, opencv_core.Mat searchFor){

        int result_cols = searchIn.cols() - searchFor.cols() + 1;
        int result_rows = searchIn.rows() - searchFor.rows() + 1;
        opencv_core.Mat result = new opencv_core.Mat(result_rows, result_cols, opencv_imgproc.COLOR_BGR2GRAY);

        opencv_core.Mat graySearchFor = new opencv_core.Mat();
        opencv_core.Mat graySearchIn = new opencv_core.Mat();
        opencv_imgproc.cvtColor(searchFor, graySearchFor, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.cvtColor(searchIn, graySearchIn, opencv_imgproc.COLOR_BGR2GRAY);

        matchTemplate(graySearchIn, graySearchFor, result, CV_TM_CCOEFF_NORMED);

        double[] min_val = new double[2];
        double[] max_val = new double[2];
        opencv_core.Point minLoc = new opencv_core.Point();
        opencv_core.Point maxLoc = new opencv_core.Point();

        opencv_core.minMaxLoc(result, min_val, max_val, minLoc, maxLoc, null);

        if( max_val[0] > 0.8 ){
            return true;
        }else{
            return false;
        }
    }

    public static opencv_core.Mat sliceImageMat(opencv_core.Mat sourceImg, int left, int top, int right, int bottom){
        opencv_core.Mat result = sourceImg.adjustROI(top,bottom,left,right);
        return result;
    }

    public static Bitmap canny(Bitmap image){
        // converters
        AndroidFrameConverter converter = new AndroidFrameConverter();
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
        opencv_core.Mat img = converterToMat.convertToMat(converter.convert(image));

        opencv_core.Mat gray = new opencv_core.Mat();
        opencv_imgproc.cvtColor(img, gray, opencv_imgproc.COLOR_BGR2GRAY);
		opencv_imgproc.blur(gray, gray, new opencv_core.Size(3, 3));
        opencv_core.Mat edges = new opencv_core.Mat();
        opencv_imgproc.Canny(gray,edges,225.0,250.0);

        Frame tempFrame = converterToMat.convert(edges);
        Bitmap edgesBitmap = converter.convert(tempFrame);

        return edgesBitmap;
    }

}
