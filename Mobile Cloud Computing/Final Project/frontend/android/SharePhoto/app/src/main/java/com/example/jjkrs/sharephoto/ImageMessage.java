package com.example.jjkrs.sharephoto;

import java.util.Comparator;

public class ImageMessage {

    private String id;
    private String username;
    private String imageUrl;
    private String lowImageUrl = "noUrl";   // defaults in case of a failure at some point
    private String highImageUrl = "noUrl";
    public boolean faceTag;    // True if contains faces

    public ImageMessage() {
    }


    public ImageMessage(String username, String imageUrl, boolean faceTag) {
        this.username = username;
        this.imageUrl = imageUrl;
        // Can't do assignment to itself here...
        //this.lowImageUrl = lowImageUrl;
        //this.highImageUrl = highImageUrl;
        this.faceTag = faceTag;
    }

    public ImageMessage(String username, String imageUrl, String lowImageUrl, String highImageUrl, boolean faceTag) {
        this.username = username;
        this.imageUrl = imageUrl;
        this.lowImageUrl = lowImageUrl;
        this.highImageUrl = highImageUrl;
        this.faceTag = faceTag;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return username;
    }
    public void setName(String username) {
        this.username = username;
    }

    public String getImageUrl() {   // Note: this returns the original resolution image!
        return imageUrl;
    }

    public String getLowImageUrl() {
        return  lowImageUrl;
    }

    public String getHighImageUrl() {
        return highImageUrl;
    }

    public boolean getFaceTag() { return faceTag; }

    // I advise that no sorting is done in this class (from Raine)

    /*Comparator for sorting the list by Owner Name*/
    public static Comparator<ImageMessage> OwnerNameComparator = new Comparator<ImageMessage>() {

        public int compare(ImageMessage ownerName1, ImageMessage ownerName2) {
            String Owner1 = ownerName1.getName().toUpperCase();
            String Owner2 = ownerName2.getName().toUpperCase();
            return Owner1.compareTo(Owner2);
        }
    };

}
