package com.functions;

class DecodedImage {
    private String image;

    public DecodedImage(String image) {
        this.image = image;
    }

    public String toString() {
        return image + ": is decoded";
    }
}