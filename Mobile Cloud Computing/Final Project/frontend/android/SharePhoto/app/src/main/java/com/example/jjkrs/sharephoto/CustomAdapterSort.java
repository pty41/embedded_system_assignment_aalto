package com.example.jjkrs.sharephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.List;

/**
 * Created by shan_kuan on 2017/12/6.
 */

public class CustomAdapterSort extends RecyclerView.Adapter<CustomAdapterSort.ViewHolder> {

    private final int mDefaultSpanCount;
    private List<GalleryItem> mItemList;
    private Context context;

    public CustomAdapterSort(List<GalleryItem> itemList, GridLayoutManager gridLayoutManager, int defaultSpanCount, Context context) {
        this.context = context;
        mItemList = itemList;
        mDefaultSpanCount = defaultSpanCount;
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return isHeaderType(position) ? mDefaultSpanCount : 1;
            }
        });

    }

    private boolean isHeaderType(int position) {
        return mItemList.get(position).getItemType() == GalleryItem.HEADER_ITEM_TYPE ? true : false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view;
        if(viewType == 0) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_gallery_header, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.activity_gallery_grid, viewGroup, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(isHeaderType(position)) {
            bindHeaderItem(holder, position);
        } else {
            bindGridItem(holder, position);
        }
    }

    /**
     * This method is used to bind grid item value
     *
     * @param holder
     * @param position
     */
    private void bindGridItem(ViewHolder holder, int position) {
        final GalleryGridItem item = (GalleryGridItem) mItemList.get(position);
        if (item.getPrivate_flag()) {

            Bitmap currentBitmap = BitmapFactory.decodeFile(item.getItemTitle());
            holder.imageView_upload.setImageBitmap(currentBitmap);
        }
        else {
            try {
                Picasso.with(context).load(item.getItemTitle()).into(holder.imageView_upload);

            } catch (Exception e) {
                Log.d("Data Lose...........", "data lose..........");
            }


        }
    }

    /**
     * This method is used to bind the header with the corresponding item position information
     *
     * @param holder
     * @param position
     */
    private void bindHeaderItem(ViewHolder holder, int position) {
        holder.header_title.setText(mItemList.get(position).getItemTitle());
    }

    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position).getItemType() == GalleryItem.HEADER_ITEM_TYPE ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    /**
     * This method is used to add an item into the recyclerview list
     *
     * @param item
     */

    public void addItem(GalleryItem item) {
        mItemList.add(item);
        notifyDataSetChanged();
    }

    /**
     * This method is used to remove items from the list
     *
     * @param item {@link GalleryItem}
     */
    public void removeItem(GalleryItem item) {
        mItemList.remove(item);
        notifyDataSetChanged();
    }
    public  class ViewHolder extends  RecyclerView.ViewHolder{

        public TextView header_title;
        public ImageView imageView_upload;
        public ViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.headerTitle);
            imageView_upload = (ImageView) itemView.findViewById(R.id.image_display);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int pos = getAdapterPosition();
                    final GalleryGridItem item = (GalleryGridItem) mItemList.get(pos);
                    if(pos!=RecyclerView.NO_POSITION) {
                        if (item.getPrivate_flag()) {
                            String[] FilePath = {"file://" + item.getItemTitle()};
                            new ImageViewer.Builder(context, FilePath)
                                    .setStartPosition(0)
                                    .show();
                        }
                        else {
                            String[] FilePath = {item.getItemTitle()};
                            new ImageViewer.Builder(context, FilePath)
                                    .setStartPosition(0)
                                    .show();
                        }
                    }
                }
            });

        }

    } // end of class ViewHolder
}