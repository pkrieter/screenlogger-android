package com.ifib.pkrieter.datarecorder;

import android.content.Context;
import android.graphics.Bitmap;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

public class OCRManager {
    private File root;
    private boolean isOCRinit;
    private tesseract.TessBaseAPI api;

    OCRManager(Context context){
        root = MainActivity.getMainDir(context);
        // OCR
        api = new tesseract.TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(root+"/tessdata", "eng") != 0) {
            isOCRinit = false;
            System.err.println("Could not initialize tesseract.");
        }else {
            isOCRinit = true;
        }
    }

    public boolean searchForString(String searchFor, Bitmap searchIn){

        // Open input image with leptonica library
        lept.PIX image = convertBmpToPix(searchIn);

        api.SetImage(image);
        // Get OCR result
        BytePointer outText;
        outText = api.GetUTF8Text();
        String foundText = outText.getString();

        boolean isInImage = containsIgnoreCase(foundText, searchFor);

        outText.deallocate();
        pixDestroy(image);

        return isInImage;
    }

    public String getStringFromImage(Bitmap searchIn){

        // Open input image with leptonica library
        lept.PIX image = convertBmpToPix(searchIn);

        api.SetImage(image);
        // Get OCR result
        BytePointer outText;
        outText = api.GetUTF8Text();

        String foundText = outText.getString();

        outText.deallocate();
        pixDestroy(image);

        return foundText;
    }

    public static boolean containsIgnoreCase(String str, String searchStr)     {
        if(str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }

    public void endApi(){
        // Destroy used object and release memory
        api.End();
    }

    private lept.PIX convertBmpToPix(Bitmap img){
        lept.PIX pixImage = null;
        Long timestamp = System.currentTimeMillis();
        Random r = new Random();
        int i1 = (r.nextInt(80) + 65);
        String fname = timestamp.toString() + i1;

        File file = new File(root + "/temp/");

        if (!file.exists()) {
            file.mkdirs();
        }

        File outFile = new File(file, fname);

        try {
            FileOutputStream out = new FileOutputStream(outFile);
            img.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            pixImage = pixRead(outFile.getAbsolutePath());
            if (outFile.exists()){
                outFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pixImage;
    }
}
