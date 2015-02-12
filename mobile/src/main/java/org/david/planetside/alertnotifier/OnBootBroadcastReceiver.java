package org.david.planetside.alertnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.david.planetside.alertnotifier.connection.WebsocketClient;

/**
 * Listens for on boot messages to startup the websocket listener.
 */
public class OnBootBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = OnBootBroadcastReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "Received an onBoot notification.");
    Intent serviceStartIntent = new Intent(context, WebsocketClient.class);
    context.startService(serviceStartIntent);
  }
}
