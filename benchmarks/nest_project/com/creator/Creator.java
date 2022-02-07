package com.creator;
import com.functions.DecodedImage;
import com.products.*;

public class Creator {
    JpegReader reader = new JpegReader(image);
    public static void main(String[] args) {
        DecodedImage decodedImage = new DecodedImage();
        String image = args[0];
        String format = image.substring(image.indexOf('.') + 1, (image.length()));
        if (format.equals("gif")) {
            GifReader reader = new GifReader(image);
        }
        if (format.equals("jpeg")) {
            JpegReader reader = new JpegReader(image);
        }
        assert reader != null;
        decodedImage = reader.getDecodeImage();
        System.out.println(decodedImage);
    }
}