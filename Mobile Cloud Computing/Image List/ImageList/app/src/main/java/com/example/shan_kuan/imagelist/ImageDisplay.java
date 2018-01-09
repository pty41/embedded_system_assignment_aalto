package com.example.shan_kuan.imagelist;

/**
 * Created by quocnguyen on 03/08/2016.
 */
public class ImageDisplay {
    private String image;
    private String name;

    public ImageDisplay(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}