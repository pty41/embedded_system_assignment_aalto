package com.example.jjkrs.sharephoto;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AppSettings extends AppCompatActivity {
    private ImageView wifi;
    private ImageView cell;
    private Spinner wifilist;
    private Spinner celllist;
    private Button selectimqButton;

    private String TAG_ORIGINAL = "Original";
    private String TAG_LOW = "Low";
    private String TAG_HIGH = "High";

    // Select type of image load
    /*  Default option for image load - original scaling: finalchoice = Original. */
    private String finalchoice = TAG_ORIGINAL;

    public static String TAG_WIFI = "WiFi";
    public static String TAG_MOBILE = "MobileData";

    public String getFinalchoice(){
        return finalchoice;
    }
    public void setFinalchoice(String choice){
        this.finalchoice =  choice;
    }

    public String getTAG_ORIGINAL(){
        return TAG_ORIGINAL;
    }
    public String getTAG_HIGH(){
        return TAG_HIGH;
    }
    public String getTAG_LOW(){
        return TAG_LOW;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            startActivity(new Intent(AppSettings.this, MainMenu.class));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        wifi = (ImageView) findViewById(R.id.wifi);
        cell = (ImageView) findViewById(R.id.cell);
        wifilist = (Spinner) findViewById(R.id.wifilist);
        celllist = (Spinner) findViewById(R.id.celllist);
        selectimqButton = (Button) findViewById(R.id.appsetButton);

        wifi.setImageResource(R.drawable.wifi_icon);
        cell.setImageResource(R.drawable.cellphone_icon);

        /*
        In the backend we encode Low resolution as "Low",
        *  High resolution - "High", Original -  "Original".
        */
        String[] items = new String[]{TAG_ORIGINAL, TAG_LOW, TAG_HIGH};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);

        wifilist.setAdapter(adapter);
        celllist.setAdapter(adapter);

        this.registerReceiver(this.mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            if(currentNetworkInfo.isConnected()){
                final ConnectivityManager connMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                final android.net.NetworkInfo wifi = connMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                final android.net.NetworkInfo mobile = connMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                String networkStatus="";
                if (wifi.isConnected()) {
                    //Toast.makeText(getApplicationContext(), "WiFi-Connected", Toast.LENGTH_LONG).show();
                    networkStatus=TAG_WIFI;
                }
                if (mobile.isConnected()) {
                    //Toast.makeText(getApplicationContext(), "Mobile-Connected", Toast.LENGTH_LONG).show();
                    networkStatus=TAG_MOBILE;
                }

                final String finalNetworkStatus = networkStatus;
            //    Toast.makeText(getApplicationContext(), networkStatus, Toast.LENGTH_LONG).show();
                selectimqButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int wposition = wifilist.getSelectedItemPosition();
                        int cposition = celllist.getSelectedItemPosition();
                        Context context = getApplicationContext();

                        //Toast.makeText(getApplicationContext(), "Connected" + finalNetworkStatus, Toast.LENGTH_LONG).show();
                        if (Objects.equals(finalNetworkStatus, TAG_WIFI)) {
                            switch (wposition) {
                                case 1:
                                    setFinalchoice(TAG_LOW);
                                   // Toast.makeText(getApplicationContext(),  TAG_WIFI+finalchoice, Toast.LENGTH_LONG).show();
                                    break;
                                case 2:
                                    setFinalchoice(TAG_HIGH);
                                    //Toast.makeText(getApplicationContext(), TAG_WIFI+finalchoice, Toast.LENGTH_LONG).show();
                                   break;
                                case 0:
                                    setFinalchoice(TAG_ORIGINAL);
                                    //Toast.makeText(getApplicationContext(), TAG_WIFI+finalchoice, Toast.LENGTH_LONG).show();
                                    break;

                                default:
                                    Toast.makeText(getApplicationContext(), "Currently you are employing wifi data. However, you haven't specified the desired quality of the image load, regarding type of network.", Toast.LENGTH_LONG).show();
                                    setFinalchoice(TAG_ORIGINAL);
                                    break;
                            }
                        } else if (Objects.equals(finalNetworkStatus, TAG_MOBILE)) {
                            switch (cposition) {
                                case 1:
                                    setFinalchoice(TAG_LOW);
                                   // Toast.makeText(getApplicationContext(), TAG_MOBILE+finalchoice, Toast.LENGTH_LONG).show();
                                    break;
                                case 2:
                                   setFinalchoice(TAG_HIGH);
                                   // Toast.makeText(getApplicationContext(), TAG_MOBILE+finalchoice, Toast.LENGTH_LONG).show();
                                    break;
                                case 0:
                                    setFinalchoice(TAG_ORIGINAL);
                                   // Toast.makeText(getApplicationContext(), TAG_MOBILE+finalchoice, Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(getApplicationContext(), "Currently you are employing mobile data. However, you haven't specified the desired quality of the image load, regarding type of network.", Toast.LENGTH_LONG).show();
                                    setFinalchoice(TAG_ORIGINAL);
                                    break;

                            }
                        }
                    }
                });

            }else{
                Toast.makeText(getApplicationContext(), "Currently you do not have any network connection.  The load of the image is impossible.", Toast.LENGTH_LONG).show();
                setFinalchoice(TAG_ORIGINAL);
            }
        }
    };


}


