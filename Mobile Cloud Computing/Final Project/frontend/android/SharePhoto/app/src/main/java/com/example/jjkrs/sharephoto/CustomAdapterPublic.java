package com.example.jjkrs.sharephoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

//import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by shan_kuan on 2017/11/22.
 */

public class CustomAdapterPublic extends RecyclerView.Adapter<CustomAdapterPublic.ViewHolder> {

    private Context context;
    private List<MyAlbum> my_album;

    public CustomAdapterPublic(Context context, List<MyAlbum> my_album) {
        this.context = context;
        this.my_album = my_album;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_gallery_public_cardview, parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            Picasso.with(context).load(my_album.get(position).getImage_link()).into(holder.imageView);
            Drawable myDrawable = context.getResources().getDrawable(R.mipmap.ic_shortcut_cloud_done);
            holder.imageView_upload.setImageDrawable(myDrawable);
            holder.pic_number.setText(Integer.toString(my_album.get(position).getPic_number()));
            holder.group_name.setText(my_album.get(position).getCatagory());
            holder.currentItem = my_album.get(position);
        }
            catch (Exception e) {
            e.printStackTrace();
            }

    }

    @Override
    public int getItemCount() {
        return my_album.size();
    }


    public  class ViewHolder extends  RecyclerView.ViewHolder{

        public TextView pic_number;
        public TextView group_name;
        public ImageView imageView;
        public ImageView imageView_upload;
        public MyAlbum currentItem;
        int REQUEST_IMAGE_LOAD = 2;

        public ViewHolder(View itemView) {
            super(itemView);
            pic_number = (TextView) itemView.findViewById(R.id.pic_number_public);
            group_name = (TextView) itemView.findViewById(R.id.group_name);
            imageView = (ImageView) itemView.findViewById(R.id.image_public);
            imageView_upload = (ImageView) itemView.findViewById(R.id.image_upload_public);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(context, GallerySort.class);
                        intent.putExtra("group_name", my_album.get(pos).getCatagory());
                        context.startActivity(intent);
                    }

                }
            });
        }
    } // end of class ViewHolder

}
