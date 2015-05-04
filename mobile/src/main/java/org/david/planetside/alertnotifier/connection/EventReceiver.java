package org.david.planetside.alertnotifier.connection;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.david.planetside.alertnotifier.AlertNotifierApplication;
import org.david.planetside.alertnotifier.controller.NotificationCreator;
import org.david.planetside.alertnotifier.model.ServerAlert;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Websocket message receiver.
 */
public class EventReceiver extends Endpoint {
  private static final String TAG = EventReceiver.class.getSimpleName();
  private static final int RECONNECT_INTERVAL_MS = 120 * 1000;
  private final AlertNotifierApplication application;
  private final PrintWriter connectionLogWriter;
  private final Handler uiThreadHandler;
  private MessageHandler.Whole<String> messageHandler = new MessageHandler.Whole<String>() {
    @Override
    public void onMessage(String message) {
      Log.d(TAG, "Received message from server: " + message);

      // Construct the ServerAlert object
      ServerAlert serverAlert;
      try {
        RESTClient restClient = new RESTClient();
        serverAlert = ServerAlert.parseFromWebsocketPayload(new JSONObject(message),
            application.getServerList(restClient),
            application.getContinentList(restClient),
            application.getFactionList(restClient), restClient);
      } catch (JSONException e) {
        Log.w(TAG, "JSON payload from server is incorrectly formed or not an alert event.");
        return;
      } catch (IOException e) {
        Log.e(TAG, "Error retrieving required data from server.", e);
        writeToPersistentLog("Error retrieving required data from server.", e);
        return;
      }

      Context context = application.getApplicationContext();

      if (serverAlert.isActive()) {
        // Add the new server alert.
        application.addServerAlert(serverAlert);

        // Send an alert notification
        NotificationCreator notificationCreator = new NotificationCreator();
        notificationCreator.createNotification(context, serverAlert, uiThreadHandler);
      } else {
        // Remove the server alert.
        NotificationCreator notificationCreator = new NotificationCreator();
        notificationCreator.cancelNotification(context, serverAlert.getServer().getServerId());
        if (!application.getServerAlerts().contains(serverAlert)) {
          // If the alert is not in the list then don't bother removing and sending an alert
          // updated intent.
          return;
        }
        application.getServerAlerts().remove(serverAlert);
      }

      LocalBroadcastManager localBroadcastManager =
          LocalBroadcastManager.getInstance(context);
      localBroadcastManager.sendBroadcast(new Intent("ALERT_DATA_UPDATED"));
    }
  };

  public EventReceiver(AlertNotifierApplication application,
                       PrintWriter connectionLogWriter, Handler uiThreadHandler) {
    this.uiThreadHandler = uiThreadHandler;
    this.application = application;
    this.connectionLogWriter = connectionLogWriter;
  }

  @Override
  public void onOpen(Session session, EndpointConfig config) {
    Log.d(TAG, "Connection opened!");
    writeToPersistentLog("Connection opened!", null);
    session.addMessageHandler(messageHandler);
  }

  @Override
  public void onClose(Session session, CloseReason closeReason) {
    Log.i(TAG, "Connection closed! Reason: " + closeReason);
    writeToPersistentLog("Connection closed. Reason: " + closeReason.getReasonPhrase()
        + "(" + closeReason.getCloseCode() + ")", null);

    // Try reconnecting every 2 minutes.
    if (session != null) {
      tryReconnecting(session);
    }
  }

  @Override
  public void onError(Session session, Throwable throwable) {
    Log.e(TAG, "Connection error!", throwable);
    writeToPersistentLog("Connection error!", throwable);

    // Try reconnecting every 2 minutes.
    if (session != null) {
      tryReconnecting(session);
    }
  }

  /**
   * Try connecting to the server every 2 minutes until connection succeeds.
   */
  private void tryReconnecting(final Session session) {
    final Endpoint thisEndpoint = this;
    Thread reconnectThread = new Thread(new Runnable() {
      @Override
      public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0xFF);

        Log.i(TAG, "Trying to reconnect. Is the session already open? " + session.isOpen());
        writeToPersistentLog("Trying to reconnect after connection closed/error. " +
            "Is the session open?" + session.isOpen(), null);
        while (!session.isOpen()) {
          try {
            Log.i(TAG, "Reconnecting...");
            writeToPersistentLog("Reconnecting...", null);

            WebSocketContainer container = session.getContainer();
            container.connectToServer(
                thisEndpoint, URI.create(WebsocketClient.WEBSOCKET_SERVER_ADDRESS));

            Thread.sleep(RECONNECT_INTERVAL_MS);
          } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while trying to reconnect.", e);
            writeToPersistentLog("Interrupted while trying to reconnect.", e);
            break;
          } catch (DeploymentException | IOException e) {
            Log.e(TAG, "Exception while trying to reconnect.", e);
            writeToPersistentLog("Exception while trying to reconnect.", e);
            break;
          }
        }
      }
    });
    reconnectThread.start();
  }

  private void writeToPersistentLog(String message, Throwable exception) {
    if (connectionLogWriter == null) {
      return;
    }

    if (message != null) {
      connectionLogWriter.append(message + "\n");
    }
    if (exception != null) {
      connectionLogWriter.append(Log.getStackTraceString(exception) + "\n");
    }

    connectionLogWriter.flush();
  }
}
