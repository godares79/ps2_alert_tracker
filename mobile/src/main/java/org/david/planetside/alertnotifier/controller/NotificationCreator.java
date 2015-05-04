package org.david.planetside.alertnotifier.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import org.david.planetside.alertnotifier.R;
import org.david.planetside.alertnotifier.model.ServerAlert;
import org.david.planetside.alertnotifier.ui.MainActivity;

import java.util.Date;

/**
 * Utility class for posting notifications to android.
 */
public class NotificationCreator {

  public void createNotification(
      final Context context, final ServerAlert serverAlert, final Handler uiThreadHandler) {
    final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.alert_notification);
    remoteView.setTextViewText(R.id.server_name_textview, serverAlert.getServer().getServerName());
    remoteView.setTextViewText(R.id.continent_name_textview, serverAlert.getContinent().getName());

    final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.alert_icon)
        .setContentTitle("Received alert!")
        .setContent(remoteView);

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

    final NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(serverAlert.getServer().getServerId(), builder.build());

    // Create a timer for updating the countdown textview.
    final Date endTime = ServerAlert.getFinishTime(serverAlert.getAlertStartTime());
    final long msUntilFinished = ServerAlert.getMsUntilAlertFinished(endTime);
    uiThreadHandler.post(new Runnable() {
      @Override
      public void run() {
        CountDownTimer countDownTimer = new CountDownTimer(msUntilFinished, 1000) {
          @Override
          public void onTick(long millisUntilFinished) {
            long secondsUntilFinished = millisUntilFinished / 1000;
            double hours = Math.floor(secondsUntilFinished / (60 * 60));
            double minutes = Math.floor((secondsUntilFinished % (60 * 60)) / 60);
            double seconds = Math.ceil((secondsUntilFinished % (60 * 60)) % 60);

            String timeString = "";
            if (hours == 0) {
              timeString = "0";
            }
            timeString += (int) hours + ":";

            if (minutes < 10) {
              timeString += "0";
            }
            timeString += (int) minutes + ":";

            if (seconds < 10) {
              timeString += "0";
            }
            timeString += (int) seconds;

            remoteView.setTextViewText(R.id.alert_timer_textview, timeString);
            builder.setContent(remoteView);
            notificationManager.notify(serverAlert.getServer().getServerId(), builder.build());
          }

          @Override
          public void onFinish() {
            // Set to complete. But it will be removed soon anyways.
            remoteView.setTextViewText(R.id.alert_timer_textview, "COMPLETE");
            builder.setContent(remoteView);
            notificationManager.notify(serverAlert.getServer().getServerId(), builder.build());
          }
        };
        countDownTimer.start();
      }
    });
  }

  public void cancelNotification(Context context, int serverId) {
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(serverId);
  }
}
