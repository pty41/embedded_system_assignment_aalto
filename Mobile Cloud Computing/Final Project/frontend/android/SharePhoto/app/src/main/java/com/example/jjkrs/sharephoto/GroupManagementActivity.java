package com.example.jjkrs.sharephoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupManagementActivity extends AppCompatActivity {


    FloatingActionMenu fam; // say no more fam
    FloatingActionButton btnAddMembers, btnCreateGroup, btnJoinGroup, btnLeaveGroup;
    TextView txtGroupName;
    TextView txtGroupExpires;
    ListView listGroupMembers;

    public String idToken;
    public String expiration_date;

    public String group_members;
    public static String joiner_token;

    // Handle back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            startActivity(new Intent(GroupManagementActivity.this, MainMenu.class));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Group Management");



        fam = (FloatingActionMenu) findViewById(R.id.fab_menu);
        btnAddMembers = (FloatingActionButton) findViewById(R.id.btnAddMembers);
        btnCreateGroup = (FloatingActionButton) findViewById(R.id.btnCreateGroup);
        btnJoinGroup = (FloatingActionButton) findViewById(R.id.btnJoinGroup);
        btnLeaveGroup = (FloatingActionButton) findViewById(R.id.btnLeaveGroup);

        txtGroupName = (TextView) findViewById(R.id.txtGroupName);
        txtGroupExpires = (TextView) findViewById(R.id.txtGroupExpires);

        listGroupMembers = (ListView) findViewById(R.id.listGroupMembers);

        // TODO get group data from backend -> set group name, group expires and group members -texts

        // Get IdToken from firebase
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if user is not signed in
        if (mUser == null) {
            // Not signed in, go to sign up form
            startActivity(SignInActivity.createIntent(this));
            finish();
            return;
        }

        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken = task.getResult().getToken();
                            new GetInformation().execute("https://mcc-fall-2017-g10.appspot.com/groups");


                        } else {

                        }
                    }
                });


        btnAddMembers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(GroupManagementActivity.this, AddMembersActivity.class));

                }
            });



        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              //  new GetInformation().execute("https://mcc-fall-2017-g10.appspot.com/groups");
                leave_alert("Creating a new group removes your current group. Are you sure?", "yes", "no");

            }
        });


        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                leave_alert("You will be removed from your current group. Are you sure?", "yes", "no");



            }
        });


        btnLeaveGroup.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                leave_alert("Are you sure you want to leave this group?", "Yes", "No");

                }
            });

    }


    // Display leave alert, handle results
    public void leave_alert(String message, String positive_value, String negative_value) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positive_value, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        new leaveGroup().execute("https://mcc-fall-2017-g10.appspot.com/groups");
                        MainMenu.user_has_a_group = false;
                        startActivity(new Intent(GroupManagementActivity.this, MainMenu.class));
                    }
                })
                .setNegativeButton(negative_value, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private class GetInformation extends AsyncTask<String, Void, String> {

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
                jsonObject.accumulate("action", "information");
                jsonObject.accumulate("id_token", idToken);
                json = jsonObject.toString();

                Log.i("token", idToken.toString());

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

            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject obj = new JSONObject(result);
                expiration_date = obj.getString("expiration_date");

                // TODO loop all members, this is just one member
                group_members = obj.getString("member_list");

                joiner_token = obj.getString("joiner_token");
                Log.e("joiner token", joiner_token);

                // temp
                String member_string = group_members.substring(group_members.indexOf("[")+2, group_members.indexOf("]")-1);
                String[] members = new String[] {member_string};

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupManagementActivity.this, android.R.layout.simple_list_item_1, members);
                listGroupMembers.setAdapter(adapter);

                txtGroupName.setText(MainMenu.getMyActiveGroupName());
                txtGroupExpires.setText(expiration_date);


            } catch (JSONException e){
                e.printStackTrace();
            }


        }

    }


    private class leaveGroup extends AsyncTask<String, Void, String> {

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
                jsonObject.accumulate("action", "leave");
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

            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            MainMenu.user_has_a_group = false;
            startActivity(new Intent(GroupManagementActivity.this, MainMenu.class));

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


