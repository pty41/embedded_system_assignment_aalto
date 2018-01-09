package com.example.jjkrs.sharephoto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shan_kuan on 2017/12/6.
 */

public class GalleryPrivate extends AppCompatActivity {

    private static final int DEFAULT_SPAN_COUNT = 3;
    private RecyclerView mRecyclerView;
    public CustomAdapterSort mAdapter;
    private List<GalleryItem> mItemList= new ArrayList<>();
    public GridLayoutManager gridLayoutManager;
    private int mHeaderCounter = 0;
    private int mGridCounter;
    public String folder_name = "/storage/emulated/0/Android/data/com.example.jjkrs.sharephoto/files/Pictures/Private";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_main);
        Fresco.initialize(this);
        configViews();
    }

    private void configViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new CustomAdapterSort(mItemList, gridLayoutManager, DEFAULT_SPAN_COUNT, this);
        mRecyclerView.setAdapter(mAdapter);
        addMockList();
    }


    private void addMockList() {
        String[] fileNames = PickPicture();
        mAdapter.addItem(new GalleryHeaderItem("Gallery"));
        if (fileNames==null)
            return;
        for (int i = 0; i < fileNames.length; i++) {
            Log.d("IMAGE_PATH", folder_name+fileNames[i]);
            mAdapter.addItem(new GalleryGridItem(folder_name+"/"+fileNames[i], getGridCounter(), true));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    //@Override
    public void onClick(View view) {
    }

    public int getHeaderCounter() {
        mGridCounter = 0;
        return ++mHeaderCounter;
    }

    public int getGridCounter() {
        return ++mGridCounter;
    }

    private String[] PickPicture() {
        ArrayList<String> imagesPath = new ArrayList<String>();
        String[] fileNames=null;
        File path = new File(folder_name);
        if(path.exists())
        {
            fileNames = path.list();
        }
        else
        {
            path.mkdir();
        }
        return fileNames;

    }
}
