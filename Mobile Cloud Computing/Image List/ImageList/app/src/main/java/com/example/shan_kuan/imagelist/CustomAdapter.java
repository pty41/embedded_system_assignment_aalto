package com.example.shan_kuan.imagelist;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class CustomAdapter extends ArrayAdapter<ImageDisplay> {

    ArrayList<ImageDisplay> products;
    Context context;
    int resource;

    public CustomAdapter(Context context, int resource, ArrayList<ImageDisplay> products) {
        super(context, resource, products);
        this.products = products;
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_image, null, true);

        }
        ImageDisplay display_image = getItem(position);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        Picasso.with(context).load(display_image.getImage()).into(imageView);

        TextView txtName = (TextView) convertView.findViewById(R.id.textView2);
        txtName.setText(display_image.getName());
        System.out.println(display_image.getName());
        return convertView;
    }
}