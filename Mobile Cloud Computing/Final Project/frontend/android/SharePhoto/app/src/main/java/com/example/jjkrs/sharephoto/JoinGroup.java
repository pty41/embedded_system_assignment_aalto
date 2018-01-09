package com.example.jjkrs.sharephoto;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class JoinGroup extends AppCompatActivity {

    private SurfaceView cameraView;
    public String idToken;
    public String qr_token;
    private static final int REQUEST_CAMERA_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        ActionBar bar = getSupportActionBar();
       // bar.hide();
        bar.setTitle("Looking for QR-codes..");
        cameraView = (SurfaceView) findViewById(R.id.cameraView);

        // set up barcode detector
        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();
        if(!detector.isOperational()){
            // Add toast "could not set up barcode detector)
            return;
        }

        // Set up camera into the surface view
        final CameraSource cameraSource = new CameraSource
                .Builder(this, detector)
                .setRequestedPreviewSize(1280, 720)
                .build();


        // Get IdToken from firebase
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken = task.getResult().getToken();

                        } else {

                        }
                    }
                });


        // Set up camera view, check for permission
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                ActivityCompat.requestPermissions(JoinGroup.this, new String[]{ android.Manifest.permission.CAMERA}, 1);
                try {

                    if(ContextCompat.checkSelfPermission(JoinGroup.this,
                            android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(cameraView.getHolder());
                    }

                } catch (IOException ie) {
                    // Add toast "Camera error"

                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        // Detect barcodes in real-time
         detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> found_barcodes = detections.getDetectedItems();

                if (found_barcodes.size() != 0) {

                    Barcode token = found_barcodes.valueAt(0);
                    qr_token = token.rawValue;

                    new joinGroup().execute("https://mcc-fall-2017-g10.appspot.com/groups");

                    }
                }

        });
    }

    private class joinGroup extends AsyncTask<String, Void, String> {

        // Send information about joining a group to backend

        protected String doInBackground(String... params) {

            String answer = "";
            InputStream inputStream = null;
            URLConnection connection;
            DataOutputStream dataOut;
            BufferedReader reader = null;


            try {

                // POST to backend
                HttpClient httpclient = new DefaultHttpClient();

                HttpPost post = new HttpPost(params[0]);

                // Define JSON
                String json;
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("action", "join");
                jsonObject.accumulate("id_token", idToken);
                jsonObject.accumulate("group_token", qr_token);
                json = jsonObject.toString();

                Log.i("JSON", json);
                Log.i("joiner token", qr_token);

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

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            Log.i("Result", answer);
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // if result == "true" -> joinerToken = joiner_token, user_has_a_group = true ...
            // else -> restart JoinGroup activity

            MainMenu.user_has_a_group = true;
            startActivity(new Intent(JoinGroup.this, GroupManagementActivity.class));

        }

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
