package com.example.jjkrs.sharephoto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by shan_kuan on 2017/11/29.
 */

public  class ViewHolder extends  RecyclerView.ViewHolder{

    public TextView pic_number;
    public TextView catagory;
    public ImageView imageView;
    public ImageView imageView_upload;
    // public CardView cardview;

    //Activity activity;

    public ViewHolder(View itemView) {
        super(itemView);
        pic_number = (TextView) itemView.findViewById(R.id.pic_number);
        catagory = (TextView) itemView.findViewById(R.id.catagory);
        imageView = (ImageView) itemView.findViewById(R.id.image);
        imageView_upload = (ImageView) itemView.findViewById(R.id.image_upload);
        //cardview = (CardView) itemView.findViewById(R.id.card_view);
            /*
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("Andriod/data/com.example.jjkrs.sharephoto/files/Pictures/");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        activity.startActivityForResult(Intent.createChooser(intent, "Select Contact Image"), REQUEST_IMAGE_LOAD);
                        //startActivityForResult(Intent.createChooser(pickPhoto, "Select Contact Image"), REQUEST_IMAGE_LOAD);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            */
    }

} // end of class ViewHolder

