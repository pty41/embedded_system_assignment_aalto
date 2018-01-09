package com.example.shan_kuan.imagelist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ArrayList<ImageDisplay> arrayList;
    EditText txtUrl;
    Button btnLoadImg;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arrayList = new ArrayList<>();
        txtUrl = (EditText)findViewById(R.id.txtUrl);
        btnLoadImg = (Button)findViewById(R.id.btnLoadImg);
        lv = (ListView) findViewById(R.id.listView1);

        //try {
            btnLoadImg.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
               //     */
                    //final CountDownLatch latch = new CountDownLatch(1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new ReadJSON().execute(txtUrl.getText().toString());
                         //   new ReadJSON().execute("http://www.mocky.io/v2/59a94ceb100000200c3e0a78");
                            //latch.countDown();
                        }
                    });

        /*
                    try {
                        //TimeUnit.SECONDS.sleep(3);
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */


                    //ExecutorService service =  Executors.newSingleThreadExecutor();
                }

            });


       // }
       // catch (Exception e) {};




    }


    class ReadJSON extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            return readURL(params[0]);
        }

        @Override
        protected void onPostExecute(String content) {
            try {
                JSONArray jsonArray = new JSONArray(content);
                System.out.println("TEST111");
                for(int i =0;i<jsonArray.length(); i++){

                    JSONObject displayObject = jsonArray.getJSONObject(i);
                    System.out.println(displayObject.getString("author"));
                    arrayList.add(new ImageDisplay(
                            displayObject.getString("photo"),
                            displayObject.getString("author")
                    ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            CustomAdapter adapter = new CustomAdapter(
                    getApplicationContext(), R.layout.listview_image, arrayList
            );
            lv.setAdapter(adapter);
        }
    }


    private static String readURL(String theUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(theUrl)
                    .build();
        } catch (Exception e) {
            System.out.println("failed.sdsd................");
            return null;
        }
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    }
