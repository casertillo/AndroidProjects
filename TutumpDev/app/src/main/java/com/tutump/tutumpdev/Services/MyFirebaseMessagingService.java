package com.tutump.tutumpdev.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tutump.tutumpdev.Activities.TabsActivity;
import com.tutump.tutumpdev.R;

/**
 * Created by casertillo on 08/08/16.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFMService";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder notificationBuilder;
    public static int numMessages = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d(TAG, "FCM From Message: " + remoteMessage.getFrom());
        Log.d(TAG, "FCM Data Message: " + remoteMessage.getNotification().getBody());
        Log.d(TAG, "REMOTE MESSAGE FULL: "+ remoteMessage.toString());

        sendNotification(remoteMessage.getNotification().getBody());
    }
    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, TabsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyID = 1;

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Tutump")
                .setContentText(messageBody)
                .setNumber(++numMessages)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        Log.d(TAG, "SERVICE NOTIFICATION" + Integer.toString(numMessages));
        mNotificationManager.notify(notifyID, notificationBuilder.build());

        //NotificationManager notificationManager =
        //        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //notificationManager.notify(0, notificationBuilder.build());
    }
}
