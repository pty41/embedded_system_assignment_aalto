package com.example.jjkrs.sharephoto;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by shan_kuan on 2017/12/6.
 */

public class GallerySort extends AppCompatActivity {

    private static final int DEFAULT_SPAN_COUNT = 3;
    private RecyclerView mRecyclerView;
    private CustomAdapterSort mAdapter;
    private List<GalleryItem> mItemList = new ArrayList<>();
    private int mHeaderCounter = 0;
    private int mGridCounter;
    public boolean [] PrivateAlbum;
    public boolean PrivateAlbum_display = true;
    public static final String GROUP_IMAGE_CHAIN = "group_image_chain_mcc-g10";
    private RecyclerView recyclerView;
    private List<ImageMessage> data_list = new ArrayList<>();
    private GridLayoutManager gridLayoutManager;
    private CustomAdapter adapter;
    private Bitmap currentBitmap;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private String mUsername = "unknown";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private String userID;
    private String NowGroupId;
    private int SortStatus = 1;
    private boolean NotFirstCreate = false;
    private boolean UpdateAuto = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_display);
        Fresco.initialize(this);
        NowGroupId = getIntent().getExtras().getString("group_name");
        configViews();
    }

    private void configViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_display);
        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference().child(GROUP_IMAGE_CHAIN);

        user = mAuth.getCurrentUser();
        userID = user.getUid();
        // Check if user is not signed in
        if (user == null) {
            // Not signed in, go to sign up form
            startActivity(SignInActivity.createIntent(this));
            finish();
            return;
        } else {
            mUsername = user.getDisplayName();
            // GROUP_ID = user.getGroupID(); TODO
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                }
            }
        };
        mAdapter = new CustomAdapterSort(mItemList, gridLayoutManager, DEFAULT_SPAN_COUNT, this);
        mRecyclerView.setAdapter(mAdapter);
        addMockList(1);
        //NotFirstCreate = true;
    }

/*
    private void showData (DataSnapshot dataSnapshot) {
        data_list = new ArrayList<>();
        DataSnapshot dd1 = dataSnapshot.child(NowGroupId);
        ImageMessage data = null;
        for (DataSnapshot ds : dd1.getChildren()) {
            data = new ImageMessage(ds.getValue(ImageMessage.class).getName(), ds.getValue(ImageMessage.class).getImageUrl(), ds.getValue(ImageMessage.class).getFaceTag());
            data_list.add(data);

        }
    }
    */




    private void addMockList(final int SortOption) {
      //  /*
        SortStatus = SortOption;
        UpdateAuto = true;
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!UpdateAuto) {
                    mItemList.clear();
                    mAdapter.notifyDataSetChanged();
                }
                data_list = new ArrayList<>();
                DataSnapshot dd1 = dataSnapshot.child(NowGroupId);
                ImageMessage data = null;
                for (DataSnapshot ds : dd1.getChildren()) {
                    data = new ImageMessage(ds.getValue(ImageMessage.class).getName(), ds.getValue(ImageMessage.class).getImageUrl(), ds.getValue(ImageMessage.class).getFaceTag());
                    data_list.add(data);

                }
                MenuList(SortStatus);

                if (UpdateAuto) {
                    UpdateAuto = false;
                }
               // if (!NotFirstCreate) {
                //    NotFirstCreate = true;
               // }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
       // */
/*
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                 @Override
                                                 public void onDataChange(DataSnapshot dataSnapshot) {
                                                     data_list = new ArrayList<>();
                                                     DataSnapshot dd1 = dataSnapshot.child(NowGroupId);
                                                     ImageMessage data = null;
                                                     for (DataSnapshot ds : dd1.getChildren()) {
                                                         data = new ImageMessage(ds.getValue(ImageMessage.class).getName(), ds.getValue(ImageMessage.class).getImageUrl(), ds.getValue(ImageMessage.class).getFaceTag());
                                                         data_list.add(data);

                                                     }
                                                     MenuList(SortOption);
                                                 }

                                                 @Override
                                                 public void onCancelled(DatabaseError databaseError) {

                                                 }
                                             }

        );
        */

    }

    private void MenuList(int SortOption) {
        if (SortOption == 2) {
            Collections.sort(data_list, ImageMessage.OwnerNameComparator);
            String PreOwnerName = "";
            String NowOwnerName = "";
            for (int i = 0; i < data_list.size(); i++) {
                NowOwnerName = data_list.get(i).getName();
                if (!PreOwnerName.equals(NowOwnerName)) {
                    PreOwnerName = NowOwnerName;
                    mAdapter.addItem(new GalleryHeaderItem(PreOwnerName));
                }
                mAdapter.addItem(new GalleryGridItem(data_list.get(i).getImageUrl(), getGridCounter(), false));

            }
        }
        else if (SortOption == 3) {
            int flag=1;
            Collections.sort(data_list, new Comparator<ImageMessage>() {
                @Override
                public int compare(ImageMessage face1, ImageMessage face2) {
                    return Boolean.compare(face2.faceTag, face1.faceTag);
                }
            }
            );
            Boolean Isface = false;
            mAdapter.addItem(new GalleryHeaderItem("No People"));
            for (int i = 0; i < data_list.size(); i++) {
                Isface = data_list.get(i).getFaceTag();
                if (Isface) {
                    flag=0;
                    mAdapter.addItem(new GalleryHeaderItem("People"));
                }
                mAdapter.addItem(new GalleryGridItem(data_list.get(i).getImageUrl(), getGridCounter(), false));

            }
            //if (flag ==1)
            //    mAdapter.addItem(new GalleryHeaderItem("People"));

        }
        else {
            mAdapter.addItem(new GalleryHeaderItem("Gallery"));
            for (int i = 0; i < data_list.size(); i++) {
                mAdapter.addItem(new GalleryGridItem(data_list.get(i).getImageUrl(), getGridCounter(), false));
            }


        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_menue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        switch (id){
            case R.id.SortOwner:
                addMockList(2);
                break;

            case R.id.SortPeople:
                addMockList(3);
                break;
            case R.id.CloseSort:
                addMockList(1);
                break;
            default:
                break;

        }
        mItemList.clear();
        mAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
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

}
