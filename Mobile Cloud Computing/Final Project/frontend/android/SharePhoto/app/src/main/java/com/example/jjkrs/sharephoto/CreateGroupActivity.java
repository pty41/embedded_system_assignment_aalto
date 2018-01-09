package com.example.jjkrs.sharephoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

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
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Calendar;

import java.sql.Time;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission_group.CALENDAR;

public class CreateGroupActivity extends AppCompatActivity {

    private Button btnAccept;
    private Button btnDecline;
    public static TimePicker timePicker;
    public static DatePicker datePicker;
    public static TextView groupName;
    private ConstraintLayout constrlayout;
    public String idToken;
    public long epoch_time;
    static public String joinerToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Create a new Group");


        constrlayout = (ConstraintLayout) findViewById(R.id.constrlayout);

        btnAccept = (Button) findViewById(R.id.btnAcceptGroup);
        btnDecline = (Button) findViewById(R.id.btnDiscardGroup);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        datePicker = (DatePicker) findViewById(R.id.datePicker);

        // Min date should be present day
        datePicker.setMinDate(System.currentTimeMillis() - 1000);

        // Set 24h format
        timePicker.setIs24HourView(true);

        groupName = (TextView) findViewById(R.id.txtName);


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

                        } else {

                        }
                    }
                });



        // Decline button -> Discard changes, go back to main menu
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateGroupActivity.this, MainMenu.class));

            }
        });


        // Accept button -> Save changes and view the newly created group
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Save expiration data
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();

                // Get current date
                Calendar calendar = Calendar.getInstance();

                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH) + 1;
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);


                calendar.set(year, month-1, day, hour+2, minute);
               // calendar.set(calendar.HOUR_OF_DAY, hour);
               // calendar.set(calendar.MINUTE, minute);

                long time = calendar.getTimeInMillis() / 1000;

                epoch_time = time;

                Log.i("epoch", Long.toString(epoch_time));

                // Save group name
                String group_name = groupName.getText().toString();

                if(day == currentDay && month == currentMonth && year == currentYear) {

                    if (hour > new Time(System.currentTimeMillis()).getHours()) {
                        new createGroup().execute("https://mcc-fall-2017-g10.appspot.com/groups");

                    } else if (hour == new Time(System.currentTimeMillis()).getHours()) {

                        if (minute > new Time(System.currentTimeMillis()).getMinutes()) {
                            new createGroup().execute("https://mcc-fall-2017-g10.appspot.com/groups");

                        } else {
                            Snackbar snackbar = Snackbar
                                    .make(constrlayout, "Expiration date has to be a future date", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(constrlayout, "Expiration date has to be a future date", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }

                } else {
                    new createGroup().execute("https://mcc-fall-2017-g10.appspot.com/groups");
                }
            }
        });

    }

    public class createGroup extends AsyncTask<String, Void, String> {

        // Send group information to backend server

        protected String doInBackground(String... params) {
            String answer = "";
            InputStream inputStream = null;
            try {

                // POST to backend
                HttpClient httpclient = new DefaultHttpClient();

                HttpPost post = new HttpPost(params[0]);

                // Define JSON
                String json;
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("action", "create");
                jsonObject.accumulate("id_token", idToken);

                Log.i("id_token", idToken);

                jsonObject.accumulate("group_name", CreateGroupActivity.groupName.getText().toString());
                jsonObject.accumulate("group_time",epoch_time);
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

             Log.i("Result", answer);
             return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Save joiner token sent by backend
            joinerToken = result;

            // View newly created group in group management
            MainMenu.user_has_a_group = true;
            startActivity(new Intent(CreateGroupActivity.this, GroupManagementActivity.class));

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
