package com.example.jjkrs.sharephoto;

/**
 * Created by shan_kuan on 2017/12/6.
 */

public class GalleryHeaderItem extends GalleryItem {

    public GalleryHeaderItem(String title) {
        super(title);
    }

    @Override
    public int getItemType() {
        return HEADER_ITEM_TYPE;
    }
}