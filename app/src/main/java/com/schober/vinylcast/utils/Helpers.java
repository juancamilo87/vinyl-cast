package com.schober.vinylcast.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.Formatter;

import com.schober.vinylcast.R;
import com.schober.vinylcast.service.MediaRecorderService;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.media.app.NotificationCompat.MediaStyle;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Allen on 3/26/17.
 */

public class Helpers {

    private static final String GENERIC_NOTIFICATION_CHANNEL_ID = "generic-channel";

    private Helpers(){}	// not to be instantiated

    public static void createStopNotification(MediaSessionCompat mediaSession, Service context, Class<?> serviceClass, int NOTIFICATION_ID) {
        createNotificationChannels(context);
        PendingIntent stopIntent = PendingIntent
                .getService(context, 0, getIntent(MediaRecorderService.REQUEST_TYPE_STOP, context, serviceClass),
                        PendingIntent.FLAG_CANCEL_CURRENT);

        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();
        // Start foreground service to avoid unexpected kill
        Notification notification = new NotificationCompat.Builder(context, GENERIC_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())
                .setDeleteIntent(stopIntent)
                // Add a pause button
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_stop_black_24dp, context.getString(R.string.stop),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP)))
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP)))
                .setSmallIcon(R.drawable.ic_album_black_24dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        context.startForeground(NOTIFICATION_ID, notification);
    }

    public static Intent getIntent(String requestType, Context con, Class<?> serviceClass) {
        Intent intent = new Intent(con, serviceClass);
        intent.putExtra(MediaRecorderService.REQUEST_TYPE, requestType);
        return intent;
    }

    public static String getIpAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    public static void createNotificationChannels(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Generic";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(GENERIC_NOTIFICATION_CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
