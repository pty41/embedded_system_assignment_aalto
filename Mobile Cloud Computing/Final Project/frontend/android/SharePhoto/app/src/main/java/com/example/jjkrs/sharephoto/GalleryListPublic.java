package com.example.jjkrs.sharephoto;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class GalleryListPublic extends AppCompatActivity {
    public static final String GROUP_IMAGE_CHAIN = "group_image_chain_mcc-g10";
    private RecyclerView recyclerView;
    private List<MyAlbum> data_list;
    private GridLayoutManager gridLayoutManager;
    private CustomAdapterPublic adapter;
    private Bitmap currentBitmap;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private String mUsername;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private String userID;
    private String idToken;
    private String myGroupName = null;

    // TODO this is just a dummy, should get user's group status from backend
    public static boolean user_has_a_group = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_public_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_public);
        data_list  = new ArrayList<>();
        gridLayoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(gridLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference().child(GROUP_IMAGE_CHAIN);
        mUsername = "unknown";

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
            myGroupName = MainMenu.getMyActiveGroupName();
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    toastMessage("Successfully signed in");
                } else {
                    // User is signed out
                    toastMessage("Successfully signed out");
                }
            }
        };

        myRef.child(myGroupName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (myGroupName.equals("")) {
                    return;
                }

                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showData (DataSnapshot dataSnapshot) {
        if (data_list.size() != 0) {
            data_list.clear();
            adapter.notifyDataSetChanged();
        }

        //DataSnapshot dd1 = dataSnapshot.child("x_unknown_group_id");
        //DataSnapshot dd2 = dataSnapshot.child("unknown_group_id");
        DataSnapshot dd1 = dataSnapshot;
        int image_num = (int) dd1.getChildrenCount();
        MyAlbum data = null;
        for (DataSnapshot ds : dd1.getChildren()) {
            data = new MyAlbum(image_num, myGroupName, ds.getValue(ImageMessage.class).getImageUrl());
            break;
        }
        if (data == null) {
            finish();
        }
        data_list.add(data);
        /*
        image_num = (int) dd2.getChildrenCount();
        for (DataSnapshot ds : dd2.getChildren()) {
            data = new MyAlbum(image_num, "unknown_group_id", ds.getValue(ImageMessage.class).getImageUrl());
            break;
        }


        data_list.add(data);
        */
        if (myGroupName.equals("")) {
            finish();
        }
        adapter = new CustomAdapterPublic(this, data_list); // TODO: the data list need to include the public picture.
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
