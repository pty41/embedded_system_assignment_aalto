package com.example.jjkrs.sharephoto;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by rainiemi on 11/30/17.
 */

public class MccFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MccFirebaseIIDService";
    private static final String ENGAGE_TOPIC = "new_photo";

    /**
     * The Application's current Instance ID token is no longer valid and thus a new one must be requested.
     */
    @Override
    public void onTokenRefresh() {
        // If you need to handle the generation of a token, initially or after a refresh this is
        // where you should do that.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FCM Token: " + token);

        // Once a token is generated, we subscribe to topic.
        FirebaseMessaging.getInstance().subscribeToTopic(ENGAGE_TOPIC);
    }
}
