package com.example.jjkrs.sharephoto;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by rainiemi on 11/30/17.
 */

public class MccFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MccFMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d(TAG, "FCM Notification Message: " +
                remoteMessage.getNotification());
        Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());
        // Show action in console.
        System.out.println("FCM Message Received");
    }
}
