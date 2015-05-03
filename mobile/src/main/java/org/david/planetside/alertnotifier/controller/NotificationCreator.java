package org.david.planetside.alertnotifier.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import org.david.planetside.alertnotifier.R;
import org.david.planetside.alertnotifier.ui.MainActivity;

/**
 * Utility class for posting notifications to android.
 */
public class NotificationCreator {

  public void createNotification(
      Context context, int serverId, String serverName, String continentName) {
    // TODO: Use a RemoteView and display more information (like a timer) in the alert.
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.alert_icon)
        .setContentTitle("Received alert!")
        .setContentText(serverName + " : " + continentName);

    // Set the pending intent. Also set up the back stack to properly back out of MainActivity
    // to the home screen.
    Intent resultIntent = new Intent(context, MainActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(MainActivity.class);
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    builder.setContentIntent(resultPendingIntent);

    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(serverId, builder.build());
  }

  public void cancelNotification(Context context, int serverId) {
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(serverId);
  }
}
