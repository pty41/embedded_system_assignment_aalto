package com.example.jjkrs.sharephoto;

/**
 * Created by shan_kuan on 2017/12/6.
 */

public class GalleryGridItem extends GalleryItem {

    private int mPosition;
    private String mImageLink;
    private boolean mprivate_flag;

    public GalleryGridItem(String title, int position, boolean flag) {
        super(title);
        mPosition = position;
        mprivate_flag = flag;
    }

    public String getImage_link() {
        return mImageLink;
    }

    public void setImage_link(String image_link) {
        mImageLink = image_link;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPrivate_flag(boolean tag) {
        mprivate_flag = tag;
    }

    public boolean getPrivate_flag() {
        return mprivate_flag;
    }
    @Override
    public int getItemType() {
        return GRID_ITEM_TYPE;
    }
}
