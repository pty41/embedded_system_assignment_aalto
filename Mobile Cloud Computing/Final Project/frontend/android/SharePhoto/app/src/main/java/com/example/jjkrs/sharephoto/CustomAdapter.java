package com.example.jjkrs.sharephoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.app.Activity.RESULT_OK;

//import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by shan_kuan on 2017/11/22.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private Context context;
    private List<MyAlbum> my_album;

    public CustomAdapter(Context context, List<MyAlbum> my_album) {
        this.context = context;
        this.my_album = my_album;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_gallery_cardview, parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bitmap currentBitmap = null;
        if (my_album.get(position).getCatagory() == "Private") {
            Drawable myDrawable = context.getResources().getDrawable(R.mipmap.ic_shortcut_cloud_off);
            holder.imageView_upload.setImageDrawable(myDrawable);
            if (my_album.get(position).getPic_number() == 0) {
                Drawable no_image_draw = context.getResources().getDrawable(R.mipmap.ic_no_image);
                holder.imageView.setImageDrawable(no_image_draw);
            } else {
                currentBitmap = BitmapFactory.decodeFile(my_album.get(position).getImage_link());
                holder.imageView.setImageBitmap(currentBitmap);
            }



        }
        else {
            Picasso.with(context).load(my_album.get(position).getImage_link()).into(holder.imageView);
            Drawable myDrawable = context.getResources().getDrawable(R.mipmap.ic_shortcut_cloud_done);
            holder.imageView_upload.setImageDrawable(myDrawable);
        }

        holder.pic_number.setText(Integer.toString(my_album.get(position).getPic_number()));
        holder.catagory.setText(my_album.get(position).getCatagory());
        holder.currentItem =  my_album.get(position);
    }

    @Override
    public int getItemCount() {
        return my_album.size();
    }


    public  class ViewHolder extends  RecyclerView.ViewHolder{

        public TextView pic_number;
        public TextView catagory;
        public ImageView imageView;
        public ImageView imageView_upload;
        public MyAlbum currentItem;
        public ImageView fullimage;
        int REQUEST_IMAGE_LOAD = 2;
        public String Catagory = "";

        public ViewHolder(View itemView) {
            super(itemView);
            pic_number = (TextView) itemView.findViewById(R.id.pic_number);
            catagory = (TextView) itemView.findViewById(R.id.catagory);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            imageView_upload = (ImageView) itemView.findViewById(R.id.image_upload);
            fullimage = (ImageView) itemView.findViewById(R.id.imagefullView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    //try {
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION) {
                        MyAlbum clickedDataItem = my_album.get(pos);
                        Catagory = my_album.get(pos).getCatagory();
                        if (Catagory == "Private") {
                            Intent intent = new Intent(context, GalleryPrivate.class);
                            context.startActivity(intent);

                        }
                        else {
                            Intent intent = new Intent(context, GalleryListPublic.class);
                            context.startActivity(intent);
                        }
                    }
                }
            });
        }

        private Bitmap getBitmap(Intent data) {
            Uri uri = data.getData();
            InputStream in = null;
            try {
                final int IMAGE_MAX_SIZE = 10000000; // 1.2MP
                in = context.getContentResolver().openInputStream(uri);

                // Decode image size
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);
                in.close();
                int scale = 1;
                while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) >
                        IMAGE_MAX_SIZE) {
                    scale++;
                }
                Bitmap resultBitmap = null;
                in = context.getContentResolver().openInputStream(uri);
                if (scale > 2) {
                    scale--;
                    options = new BitmapFactory.Options();
                    options.inSampleSize = scale;
                    resultBitmap = BitmapFactory.decodeStream(in, null, options);
                    int height = resultBitmap.getHeight();
                    int width = resultBitmap.getWidth();
                    double y = Math.sqrt(IMAGE_MAX_SIZE
                            / (((double) width) / height));
                    double x = (y / height) * width;

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, (int) x,
                            (int) y, true);
                    resultBitmap.recycle();
                    resultBitmap = scaledBitmap;

                    System.gc();
                } else {
                    resultBitmap = BitmapFactory.decodeStream(in);
                }
                in.close();
                resultBitmap.getHeight();
                return resultBitmap;
            } catch (IOException e) {
                return null;
            }
        } // end of private Bitmap getBitmap(Intent data) {

        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if(requestCode == REQUEST_IMAGE_LOAD && resultCode == RESULT_OK) {
                try {

                    BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
                    bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap myBitmap = getBitmap(data);
                    fullimage.setImageBitmap(myBitmap);

                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    } // end of class ViewHolder

}
