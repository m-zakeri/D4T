package com.products;
import com.functions.DecodedImage;

class GifReader {
    private DecodedImage decodedImage;

    public GifReader(String image, int size) {
        this.decodedImage = new DecodedImage(image);
    }

    public DecodedImage getDecodeImage() {
        return decodedImage;
    }

    public int getEncodeImage() {
        return 0;
    }
}