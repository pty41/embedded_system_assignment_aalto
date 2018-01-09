package com.example.jjkrs.sharephoto;

/**
 * Created by shan_kuan on 2017/12/6.
 */

public abstract class GalleryItem {

    public static final int HEADER_ITEM_TYPE = 0;
    public static final int GRID_ITEM_TYPE = 1;
    private String mItemTitle;

    public GalleryItem(String title) {
        mItemTitle = title;
    }

    public String getItemTitle() {
        return mItemTitle;
    }

    public abstract int getItemType();
}
