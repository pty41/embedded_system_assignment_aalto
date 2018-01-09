package com.example.jjkrs.sharephoto;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class GalleryList extends AppCompatActivity {
    public static final String GROUP_IMAGE_CHAIN = "group_image_chain_mcc-g10";
    private RecyclerView recyclerView;
    private List<MyAlbum> data_list;
    private GridLayoutManager gridLayoutManager;
    private CustomAdapter adapter;
    private Bitmap currentBitmap;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private String mUsername;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private String userID;
    private String idToken;
    private boolean DisplayNotifyMessage = false;
    private int NotifyNum = 0;
    private String myGroupName = null;

    public String folder_name = "/storage/emulated/0/Android/data/com.example.jjkrs.sharephoto/files/Pictures/Private";

    // TODO this is just a dummy, should get user's group status from backend
    public static boolean user_has_a_group = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
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
            // GROUP_ID = user.getGroupID(); TODO
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

                if (DisplayNotifyMessage) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(GalleryList.this)
                                    .setSmallIcon(R.mipmap.ic_exclamation_point)
                                    .setContentTitle("Firebase Update")
                                    .setContentText("The Public Album have been updated");
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(NotifyNum, mBuilder.build());
                    toastMessage("Firebase Update");
                    NotifyNum++;
                }
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                showData(dataSnapshot);
                DisplayNotifyMessage = true;
                if (NotifyNum > 100) {
                    NotifyNum = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        adapter = new CustomAdapter(this, data_list); // TODO: the data list need to include the public picture.
        recyclerView.setAdapter(adapter);

    }

    private void showData (DataSnapshot dataSnapshot) {

        data_list.clear();
        adapter.notifyDataSetChanged();

        String[] fileNames = PickPicture();
        MyAlbum data = null;
        if (fileNames!=null && fileNames.length>0) {
            data = new MyAlbum(fileNames.length, "Private", folder_name + "/" + fileNames[0]);
        } else {
            data = new MyAlbum(0, "Private", "no_image");
        }
        data_list.add(data);
        if (myGroupName.equals("")) {
            return;
        }

        DataSnapshot dd1 = dataSnapshot;
        //DataSnapshot dd2 = dataSnapshot.child("unknown_group_id");
        int image_num = (int) dd1.getChildrenCount();
        Log.d("???????????...........XXXXXXXXX:", Integer.toString(image_num));
        //MyAlbum data = null;
        //image_num += (int)dd2.getChildrenCount();
        if (image_num != 0) {
            for (DataSnapshot ds : dd1.getChildren()) {
                data = new MyAlbum(image_num, "Public", ds.getValue(ImageMessage.class).getImageUrl());
                break;
            }
            data_list.add(data);
        }
        /*
        else {
            image_num = (int) dd2.getChildrenCount();
            for (DataSnapshot ds : dd2.getChildren()) {
                data = new MyAlbum(image_num, "Public", ds.getValue(ImageMessage.class).getImageUrl());
                break;

            }
            data_list.add(data);

        }
        */
        adapter = new CustomAdapter(this, data_list); // TODO: the data list need to include the public picture.
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



    private String[] PickPicture() {
        ArrayList<String> imagesPath = new ArrayList<String>();
        String[] fileNames = null;
        File path = new File(folder_name);
        if (path.exists()) {
            //imagesPath.add(cur.getString(dataColumn));
            fileNames = path.list();
        } else {
            path.mkdir();
        }
        return fileNames;
    }
}
