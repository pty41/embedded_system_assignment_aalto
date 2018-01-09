package com.example.jjkrs.sharephoto;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import static android.content.ContentValues.TAG;

/*

TODO

 */
public class CameraActivity extends Activity {

    // For Firebase
    private FirebaseUser user;
    private String mUsername;
    public static final String GROUP_IMAGE_CHAIN = "group_image_chain_mcc-g10";
    private static List<String> GROUP_IDS;
    private Iterator<String> group_iterator;
    static final boolean defaultFaceTag = false;    // By default, image does not contain face
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private DatabaseReference mFirebaseDatabaseReference;
    private Uri photo_uri;
    StorageReference storageReference;

    private ImageView capturedImage;
    private Button acceptButton;
    private Button declineButton;
    private View mRootView;
    private ProgressDialog mProgressDialog;

    static final int request_take_photo = 1;

    private String group_id;
    private String message_id;
    private String img_url;

    public static Intent createIntent(Context context) {
        return new Intent(context, CameraActivity.class);
    }

    // check for barcodes
    private boolean contains_barcode = false;

    public Bitmap bitmap;

    // Handle back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, MainMenu.class));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUsername = "unknown";

        user = FirebaseAuth.getInstance().getCurrentUser();
        // Check if user is not signed in
        if (user == null) {
            // Not signed in, go to sign up form
            startActivity(SignInActivity.createIntent(this));
            finish();
            return;
        } else {
            mUsername = user.getDisplayName();
            // get users group ids from backend
            GROUP_IDS = new ArrayList<String>();

            final String myGroupName = MainMenu.getMyActiveGroupName();

            System.out.println("My group: " + myGroupName);

            if (myGroupName == "") {
                Toast.makeText(CameraActivity.this, "Join a group before taking and sharing a picture!", Toast.LENGTH_LONG).show();
                // Open the grid menu
                startActivity(MainMenu.createIntent(CameraActivity.this));
                return;
            }

            // Add the name of the group
            GROUP_IDS.add(myGroupName);

        }

        setContentView(R.layout.camera);

        declineButton = (Button) findViewById(R.id.btnDecline);
        acceptButton = (Button) findViewById(R.id.BtnAccept);
        capturedImage = (ImageView) findViewById(R.id.capturedImage);

        mProgressDialog = new ProgressDialog(this);

        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{android.Manifest.permission.CAMERA}, 1);

        if (ContextCompat.checkSelfPermission(CameraActivity.this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            take_photo();
        }
        // else -> toast "you dont have permission to open camera" -> startActivity(MainMenu)


    }


    public String photopath;

    public File createImageFile() throws IOException {
        // Handle image's filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "SHAREPHOTO_" + timestamp + "_";

        // Save taken pictures into app's folder in filesystem
        // (If user deletes the app, all pictures taken through this app get deleted)
        File storage_dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                filename, ".jpg", // filename
                storage_dir      // directory
        );

        // Save the file
        photopath = image.getAbsolutePath();
        return image;

    }


    private void take_photo() {

        Intent take_picture_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (take_picture_intent.resolveActivity(getPackageManager()) != null) {
            File photofile = null;

            try {
                photofile = createImageFile();
            } catch (IOException e) {
                // Error
            }

            if (photofile != null) {
                photo_uri = FileProvider.getUriForFile(this,
                        "com.example.jjkrs.sharephoto.fileprovider", photofile);
                take_picture_intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photo_uri);

                startActivityForResult(take_picture_intent, request_take_photo);
            }
        }
    }


    private void preview_image() {
        // Image should be scaled before viewing in the app to avoid CPU overload

        // Imageview dimensions are the target dimensions
        int target_width = capturedImage.getWidth();
        int target_height = capturedImage.getHeight();

        // Get bitmap dimensions
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photopath, bmOptions);
        int photo_width = bmOptions.outWidth;
        int photo_height = bmOptions.outHeight;

        // scale if possible
        int scaleFactor = 1;

        if(photo_width != 0 && photo_height != 0 && target_width != 0 && target_height != 0) {
            scaleFactor = Math.min(photo_width / target_width, photo_height / target_height);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        // create bitmap
        bitmap = BitmapFactory.decodeFile(photopath, bmOptions);

        // Set up barcode detector
        final BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();
        if (!detector.isOperational()) {
            // Add toast "could not set up barcode detector)
            return;
        }


        // rotate bitmap
        try {
            ExifInterface exif = new ExifInterface(photopath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotate_image(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotate_image(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotate_image(bitmap, 270);
                    break;
            }

        } catch (IOException exif_error) {
            // Camera software doesn't provide Exif-data -> don't rotate and continue
        }


        // Run barcode detection on background
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<Barcode> barcodes = detector.detect(frame);

                if (barcodes.toString() != "{}") {
                    contains_barcode = true;

                }
            }
        });


        if (contains_barcode == true) {
            Toast.makeText(getApplicationContext(), "contains barcode", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "doesn't contain barcode", Toast.LENGTH_SHORT).show();
        }

        capturedImage.setImageBitmap(bitmap);

    }

    // Helper function for image rotation
    public static Bitmap rotate_image(Bitmap source, float angle) {
        Matrix mx = new Matrix();
        mx.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), mx, true);
    }


    // move image to another folder
    public void move_image(String source, String target) {
        File sourceLocation= new File (source);
        File targetDir= new File (target);

        String filename = source.substring(source.lastIndexOf("/")+1);

        File targetLocation = new File(target, filename);

        Log.i("target dir:", targetDir.toString());
        Log.i("source loc:", sourceLocation.toString());
        Log.i("target loc:", targetLocation.toString());

        // First, create new folder if it doesn't already exist
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }

        try {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the image to new location
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            // Remove the image from previous location
            File fdelete = new File(photopath);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    Log.i("file Deleted :", photopath);
                } else {
                    Log.i("file not Deleted :", photopath);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == request_take_photo) {


            // Show the taken picture to user
            preview_image();


            // Let user accept the picture or proceed to take another

            // on click (accept button)
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (contains_barcode == false) {
                        // Send the picture to Firebase (If it doesn't contain a barcode)

                        mProgressDialog.setMessage("Uploading...");
                        mProgressDialog.show();

                        final Uri uri = photo_uri; // Get the uri
                        Log.d(TAG, "Uri: " + uri.toString());

                        // At this point we use a temporary image first
                        ImageMessage tempMessage = new ImageMessage(mUsername, LOADING_IMAGE_URL, LOADING_IMAGE_URL, LOADING_IMAGE_URL, defaultFaceTag);
                        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

                        group_iterator = GROUP_IDS.iterator();
                        String first_group = group_iterator.next();
                        sendToOneGroup(tempMessage, first_group, uri);


                    } else {

                        // Images with barcodes are considered private -> move it to private folder
                        move_image(photopath, "/storage/emulated/0/Android/data/com.example.jjkrs.sharephoto/files/Pictures/Private");

                        // Toast that image has saved but not send
                        Toast.makeText(CameraActivity.this, "Picture saved in Private folder.", Toast.LENGTH_LONG).show();

                        // Go back to grid menu (contains a barcode)
                        startActivity(MainMenu.createIntent(CameraActivity.this));
                    }
                }

                private void sendToOneGroup(final ImageMessage tempMessage, final String GROUP_ID, final Uri uri) {

                    System.out.println("Sending image to this group: " + GROUP_ID);

                    mFirebaseDatabaseReference.child(GROUP_IMAGE_CHAIN).child(GROUP_ID).push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(user.getUid())
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());

                                        // And now we actually send the image
                                        putImageInStorage(storageReference, uri, key, GROUP_ID, tempMessage);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                        showSnackbar("Uploading failed due to an error:\n"
                                                + databaseError.getMessage());
                                    }
                                }
                            });

                }

                // Send the actual image
                private void putImageInStorage(StorageReference storageReference, final Uri uri, final String key, final String groupID, final ImageMessage tempMessage) {
                    UploadTask uploadTask = storageReference.putFile(uri);

                    uploadTask.addOnCompleteListener(CameraActivity.this,
                            new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        // Set these variables so that they can be used onSuccess:
                                        group_id = groupID;
                                        message_id = key;

                                        img_url = task.getResult().getMetadata().getDownloadUrl().toString();

                                        ImageMessage imageMessage =
                                                new ImageMessage(mUsername,
                                                        img_url,
                                                        LOADING_IMAGE_URL,  // These urls will be changed by the backend
                                                        LOADING_IMAGE_URL,
                                                        defaultFaceTag);

                                        mFirebaseDatabaseReference.child(GROUP_IMAGE_CHAIN).child(groupID).child(key)
                                                .setValue(imageMessage);
                                    } else {
                                        Log.w(TAG, "Image upload task was not successful.",
                                                task.getException());
                                        System.out.println("Uploading failed");
                                    }
                                }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            System.out.println("Upload is " + progress + "% done");
                        }
                    }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("Upload is paused");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            System.out.println("Uploading failed: " + exception);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("Upload successful");

                            // Send request to the backend to detect faces and save different resolution images
                            new newImageSend().execute("https://mcc-fall-2017-g10.appspot.com/loadimgqlt", group_id, message_id, img_url);

                            // Check if there are more groups, where we still need to send image
                            if (group_iterator.hasNext()) {
                                // We send the image to next group
                                String next_group = group_iterator.next();
                                sendToOneGroup(tempMessage, next_group, uri);
                            } else {
                                // We are done... close the notification, toast and open main grid
                                mProgressDialog.dismiss();
                                Toast.makeText(CameraActivity.this, "Uploading done!", Toast.LENGTH_LONG).show();
                                // Open the grid menu
                                startActivity(MainMenu.createIntent(CameraActivity.this));
                            }
                        }
                    });

                }


            }); // End of button listener for sending image to Firebase


            // on click (decline button)
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Delete photo from local storage
                    File fdelete = new File(photopath);
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            System.out.println("file Deleted :" + photopath);
                        } else {
                            System.out.println("file not Deleted :" + photopath);
                        }
                    }

                    // Open camera function again to take new picture
                    onCreate(new Bundle());


                }
            });
        } else {
            // If picture wasn't taken -> go back to main menu
            startActivity(new Intent(this, MainMenu.class));
        }
    }

    /// ------------------------- Request to backend ---------------------------- ///
    public class newImageSend extends AsyncTask<String, Void, String> {

        // Notification to backend that new image is there
        protected String doInBackground(String... params) {
            String answer = "noAnswer";
            InputStream inputStream;
            try {

                // POST to backend
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(params[0]);

                // Define JSON
                String json;
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("action", "new image");
                jsonObject.accumulate("group_id", params[1]);
                jsonObject.accumulate("message_id", params[2]);
                jsonObject.accumulate("img_url", params[3]);
                json = jsonObject.toString();

                StringEntity se = new StringEntity(json);
                post.setEntity(se);

                // Set JSON headers
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-type", "application/json");

                // Get response from backend
                HttpResponse httpResponse = httpclient.execute(post);
                inputStream = httpResponse.getEntity().getContent();

                if(inputStream != null)
                    answer = convertInputStreamToString(inputStream);

            } catch (JSONException | IOException e) {
                Log.e("JSONerror or IOException", "error");
            }

            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            System.out.println("Backend answers: " + result);
            // No need to do anything...

        }

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
    /// ------------------------- Request to backend ---------------------------- ///

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's an upload in progress, save the reference so you can query it later
        if (storageReference != null) {
            outState.putString("reference", storageReference.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was an upload in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all UploadTasks under this StorageReference
        List<UploadTask> tasks = storageReference.getActiveUploadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the upload
            UploadTask task = tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot state) {
                    // Open the grid menu
                    startActivity(MainMenu.createIntent(CameraActivity.this));
                }
            });
        }
    }

    @MainThread
    private void showSnackbar(String Message) {
        mRootView = (View) findViewById(android.R.id.content);
        Snackbar.make(mRootView, Message, Snackbar.LENGTH_INDEFINITE).show();
    }

}
