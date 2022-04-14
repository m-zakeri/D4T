package com.functions;

class DecodedImage {
    private String image;

    class InnerClass
    {
        void display()
        {
            // can access static member of outer class
            System.out.println("outer_x = " + outer_x);
              
            // can also access non-static member of outer class
            System.out.println("outer_y = " + outer_y);
              
            // can also access a private member of the outer class
            System.out.println("outer_private = " + outer_private);
          
        }
    }

    public DecodedImage(String image) {
        this.image = image;
    }

    public String toString() {
        return image + ": is decoded";
    }
}