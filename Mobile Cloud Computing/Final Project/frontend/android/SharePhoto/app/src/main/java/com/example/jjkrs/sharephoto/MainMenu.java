package com.example.jjkrs.sharephoto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;


public class MainMenu extends AppCompatActivity {

    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";

    private ImageButton btnGallery;
    private ImageButton btnCamera;
    private ImageButton btnGroupManagement;
    private ImageButton btnSettings;
    private ImageButton btnNotifications;
    private View mRootView;
    private String idToken;

    public static boolean user_has_a_group = false;
    public static boolean key_used = false;


    // Made by Raine:
    private static String users_active_group = "";
    public static String getMyActiveGroupName() {
        return users_active_group;
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, MainMenu.class);
    }

    public static Intent createIntent(
            Context context,
            IdpResponse idpResponse) {

        Intent startIntent = new Intent();
        if (idpResponse != null) {
            startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse);
        }

        return startIntent.setClass(context, MainMenu.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Check if user is not signed in
        if (user == null) {
            // Not signed in, go to sign up form
            startActivity(SignInActivity.createIntent(this));
            finish();
            return;
        }

        setContentView(R.layout.activity_main_menu);

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

        btnGallery = (ImageButton) findViewById(R.id.btnGallery);
        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        btnGroupManagement = (ImageButton) findViewById(R.id.btnGroupManagement);
        btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        btnNotifications = (ImageButton) findViewById(R.id.btnNotifications);

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {

                // TODO: Add by Shan
                startActivity(new Intent(MainMenu.this, GalleryList.class));


                //showSnackbar(R.string.not_implemented);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(CameraActivity.createIntent(MainMenu.this));
            }
        });

        btnGroupManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {


                new MainMenu.checkGroupExists().execute("https://mcc-fall-2017-g10.appspot.com/groups");

                if (user_has_a_group) {
                    // Open group management
                    startActivity(new Intent(MainMenu.this, GroupManagementActivity.class));

                } else {
                    // Show dialog if user isn't in any group
                    display_alert();
                }

            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(MainMenu.this, AppSettings.class));
            }
        });

        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(UserInfoActivity.createIntent(MainMenu.this));
                //showSnackbar(R.string.not_implemented);
            }
        });


    }

    /// --------------- Every time MainMenu is opened, it checks users group ----------- ///
    @Override
    public void onStart() {
        super.onStart();

        // Added by Raine:

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Check if user is not signed in
        if (user == null) {
            // Not signed in, go to sign up form
            startActivity(SignInActivity.createIntent(this));
            finish();
            return;
        }

        System.out.println("Trying to get user group info...");

        // Generate user token
        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken = task.getResult().getToken();
                            System.out.println("Got the token: " + idToken);
                            // Ask backend my group name
                            new askMyActiveGroup().execute("http://mcc-fall-2017-g10.appspot.com/groups");
                        } else {
                            System.out.println("Error in creating token!");
                        }
                    }
                });

    }

    private class askMyActiveGroup extends AsyncTask<String, Void, String> {

        // Send information about leaving group to the backend

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
                jsonObject.accumulate("action", "check");
                jsonObject.accumulate("id_token", idToken);
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


                ////// DEBUG (prints the http request):

                HttpEntity entity = post.getEntity();
                Header[] headers = post.getAllHeaders();
                String content = EntityUtils.toString(entity);

                System.out.println("---");
                System.out.println(post.toString());
                for (Header header : headers) {
                    System.out.println(header.getName() + ": " + header.getValue());
                }
                System.out.println("---");
                System.out.println(content);
                System.out.println("---");

                ///// DEBUG end -----


            } catch (JSONException | IOException e) {
                System.out.println("Problem in creating HttpPost request!");
            }

            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            System.out.println("Our answer from the backend when my group asked: " + result);

            if (result.equals("No")) {
                user_has_a_group = false; // This should not be used in my opinion but just in case here I set the value
                users_active_group = "";
                System.out.println("User does not have an active group according to backend.");
            } else {
                user_has_a_group = true; // This should not be used in my opinion but just in case here I set the value
                users_active_group = result;
                System.out.println("User has an active group: " + users_active_group);
            }

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
    /// ----- END ----- Every time MainMenu is opened, it checks users group ----- END ------ ///



    // Options bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    // Options bar continue
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                logOut();
                return true;
            case R.id.delete_account_menu:
                deleteAccount();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(SignInActivity.createIntent(MainMenu.this));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(SignInActivity.createIntent(MainMenu.this));
                            finish();
                        } else {
                            showSnackbar(R.string.delete_account_failed);
                        }
                    }
                });
    }

    // This is just for error messages
    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        mRootView = (View) findViewById(android.R.id.content);
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    // This is an alert message if user isn't in a group
    private void display_alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You're currently not in a group. What would you like to do?")
                .setCancelable(false)
                .setPositiveButton("Join a group", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {


                        startActivity(new Intent(MainMenu.this, JoinGroup.class));

                    }
                })
                .setNegativeButton("Create new group", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Open create a new group activity
                        startActivity(new Intent(MainMenu.this, CreateGroupActivity.class));
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
    }
    private class checkGroupExists extends AsyncTask<String, Void, String> {

        // Send information about leaving group to the backend

        protected String doInBackground(String... params) {

            String answer = "";
            URLConnection connection;
            DataOutputStream dataOut;
            BufferedReader reader = null;

            InputStream inputStream = null;
            try {


                // POST to backend
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(params[0]);

                // Define JSON
                String json;
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("action", "check");
                jsonObject.accumulate("id_token", idToken);
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

            }

            Log.i("answer", answer);
            return answer;
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException{
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.i("result", result);
            if (result == "No") {

               MainMenu.user_has_a_group = false;

            } else {

                MainMenu.user_has_a_group = true;

            }
        }

    }


}
