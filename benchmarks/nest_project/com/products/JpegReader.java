package com.products;
import com.functions.DecodedImage;

class JpegReader{
    private DecodedImage decodedImage;

    public JpegReader(String image, int size) {
        decodedImage = new DecodedImage(image);
    }

    public DecodedImage getDecodeImage(int x, int y) {
        return decodedImage;
    }
}
