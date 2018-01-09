package com.example.jjkrs.sharephoto;


public class MyAlbum {
    private int pic_number;
    private String catagory, image_link;

    public MyAlbum(int pic_number, String catagory, String image_link) {
        this.pic_number = pic_number;
        this.catagory = catagory;
        this.image_link = image_link;
    }

    public int getPic_number() {
        return pic_number;
    }

    public void setPic_number(int pic_number) {
        this.pic_number = pic_number;
    }

    public String getCatagory() {
        return catagory;
    }

    public void setCatagory(String catagory) {
        this.catagory = catagory;
    }

    public String getImage_link() {
        return image_link;
    }

    public void setImage_link(String image_link) {
        this.image_link = image_link;
    }
}
