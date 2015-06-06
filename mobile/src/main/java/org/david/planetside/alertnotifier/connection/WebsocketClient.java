package org.david.planetside.alertnotifier.connection;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.david.planetside.alertnotifier.AlertNotifierApplication;
import org.david.planetside.alertnotifier.controller.NotificationCreator;
import org.david.planetside.alertnotifier.model.Continent;
import org.david.planetside.alertnotifier.model.ContinentControl;
import org.david.planetside.alertnotifier.model.FactionList;
import org.david.planetside.alertnotifier.model.Server;
import org.david.planetside.alertnotifier.model.ServerAlert;
import org.david.planetside.alertnotifier.model.ServerList;
import org.david.planetside.alertnotifier.model.ServerPopulation;
import org.david.planetside.alertnotifier.ui.MainActivity;
import org.glassfish.tyrus.client.ClientManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import javax.websocket.DeploymentException;
import javax.websocket.Session;

/**
 * Manages and sends messages to the server.
 */
public class WebsocketClient extends Service {
  static final String WEBSOCKET_SERVER_ADDRESS =
      "wss://push.planetside2.com/streaming?service-id=s:ps2alertnotificationapp";
  private static final int UPDATE_ALERT_INFO_WAIT_MS = 10 * 60 * 1000;
  private static final String PERSISTENT_LOG_NAME = "ps2_alertnotifier_connection_log.txt";
  private static final String TAG = WebsocketClient.class.getSimpleName();
  private Thread connectionThread;
  private Session dataSession;
  private PrintWriter connectionLogWriter;
  private Handler uiThreadHandler;

  private BroadcastReceiver updateAlertSubscriptionBroadcastReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          updateAlertSubscription();
        }
      };

  private Runnable updateAlertInformationRunnable =
      new Runnable() {
        @Override
        public void run() {
          AlertNotifierApplication alertNotifierApplication =
              (AlertNotifierApplication) getApplication();
          List<ServerAlert> serverAlerts = alertNotifierApplication.getServerAlerts();
          FactionList factionList;
          try {
            factionList = alertNotifierApplication.getFactionList(new RESTClient());
          } catch (IOException e) {
            Log.e(TAG, "Problem getting faction list.", e);
            return;
          }

          while (true) {
            Log.d(TAG, "Updating alert continent control and population information.");
            for (ServerAlert serverAlert : serverAlerts) {
              RESTClient restClient = new RESTClient();
              Server server = serverAlert.getServer();
              Continent continent = serverAlert.getContinent();

              try {
                // Update the control information for the alert continent
                ContinentControl newContinentControl =
                    restClient.getContinentControl(server, continent.getId(), factionList);
                serverAlert.setContinentControl(newContinentControl);

                // Update the population information for the alert server
                ServerPopulation newServerPopulation =
                    restClient.getPopulation(server, factionList);
                serverAlert.setServerPopulation(newServerPopulation);
              } catch (IOException e) {
                Log.e(TAG, "Problem updating alert information for alert: " + serverAlert, e);
                writeToPersistentLog(
                    "Problem updating alert information for alert: " + serverAlert, e);
              }
            }

            if (serverAlerts.size() > 0) {
              LocalBroadcastManager localBroadcastManager =
                  LocalBroadcastManager.getInstance(getApplicationContext());
              localBroadcastManager.sendBroadcast(new Intent("ALERT_DATA_UPDATED"));
            }

            try {
              Thread.sleep(UPDATE_ALERT_INFO_WAIT_MS);
            } catch (InterruptedException e) {
              Log.e(TAG, "Interrupted in update alert information loop.", e);
            }
          }
        }
      };

  public void onCreate() {
    super.onCreate();
    uiThreadHandler = new Handler();
  }

  @Override
  public IBinder onBind(Intent intent) {
    // Not used. Using startService() to start, see onStartCommand.
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (connectionThread != null) {
      Log.i(TAG, "Service is already running. Returning.");
      return START_STICKY;
    }

    try {
      connectionLogWriter =
          new PrintWriter(
              new BufferedWriter(
                  new FileWriter(
                      new File(Environment.getExternalStorageDirectory(), PERSISTENT_LOG_NAME))));
    } catch (IOException e) {
      Log.e(TAG, "Issue opening the persistent connection log for writing.", e);
    }

    connectionThread = new Thread(new Runnable() {
      @Override
      public void run() {
        ClientManager clientManager = ClientManager.createClient();
        try {
          dataSession = connect(clientManager, URI.create(WEBSOCKET_SERVER_ADDRESS));
        } catch (DeploymentException | IOException e) {
          Log.e(TAG, "Problem connecting to server.", e);
          writeToPersistentLog("Problem connecting to server.", e);
        }
      }
    });
    connectionThread.start();

    Thread alertUpdateThread = new Thread(updateAlertInformationRunnable);
    alertUpdateThread.start();

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    LocalBroadcastManager localBroadcastManager =
        LocalBroadcastManager.getInstance(getApplicationContext());
    localBroadcastManager.unregisterReceiver(updateAlertSubscriptionBroadcastReceiver);
  }

  /**
   * Connect to the server at the provided endpoint.
   *
   * @return The Session object if successful.
   */
  public Session connect(ClientManager clientManager, URI endpointURI)
      throws IOException, DeploymentException {
    // Connect to the server and subscribe for server updates.
    EventReceiver eventReceiverEndpoint = new EventReceiver(
        (AlertNotifierApplication) getApplication(), connectionLogWriter, uiThreadHandler);
    // TODO: Doesn't work on android L, grab a newer version of MR1 to determine if handshake is fixed.
    Session sessionObject = clientManager.connectToServer(eventReceiverEndpoint, endpointURI);

    ServerList savedServers = ServerList.getSavedServerList(
        getSharedPreferences(MainActivity.SHARED_PREFS_FILE, 0 /** Private */));
    RESTClient restClient = new RESTClient();
    AlertNotifierApplication alertNotifierApplication = (AlertNotifierApplication) getApplication();

    fetchServerAlerts(alertNotifierApplication, restClient, savedServers);
    sendAlertSubscription(sessionObject, savedServers);

    LocalBroadcastManager localBroadcastManager =
        LocalBroadcastManager.getInstance(getApplicationContext());
    localBroadcastManager.sendBroadcast(new Intent("ALERT_DATA_UPDATED"));
    localBroadcastManager.registerReceiver(
        updateAlertSubscriptionBroadcastReceiver, new IntentFilter("ALERT_SUBSCRIPTION_UPDATED"));

    return sessionObject;
  }

  public void updateAlertSubscription() {
    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        ServerList savedServers = ServerList.getSavedServerList(
            getSharedPreferences(MainActivity.SHARED_PREFS_FILE, 0 /** Private */));

        try {
          RESTClient restClient = new RESTClient();
          AlertNotifierApplication alertNotifierApplication =
              (AlertNotifierApplication) getApplication();

          fetchServerAlerts(alertNotifierApplication, restClient, savedServers);

          LocalBroadcastManager localBroadcastManager =
              LocalBroadcastManager.getInstance(getApplicationContext());
          localBroadcastManager.sendBroadcast(new Intent("ALERT_DATA_UPDATED"));
        } catch (IOException e) {
          Log.e(TAG, "Problem getting the alerts for the newly tracked servers.", e);
          writeToPersistentLog("Problem getting the alerts for the newly tracked servers.", e);
        }

        sendAlertSubscription(dataSession, savedServers);
      }
    });
  }

  private void fetchServerAlerts(
      AlertNotifierApplication alertNotifierApplication, RESTClient restClient,
      ServerList savedServers) throws IOException {
    for (Server server : savedServers) {
      ServerAlert serverAlert = restClient.getServerAlert(
          server,
          alertNotifierApplication.getServerList(restClient),
          alertNotifierApplication.getContinentList(restClient),
          alertNotifierApplication.getFactionList(restClient));

      if (serverAlert.isActive()) {
        Log.d(TAG, "Have an active server alert for: " + server.toString());
        alertNotifierApplication.addServerAlert(serverAlert);

        // Send an alert notification
        NotificationCreator notificationCreator = new NotificationCreator();
        notificationCreator.createNotification(getApplicationContext(), serverAlert, uiThreadHandler);
      }
    }

    alertNotifierApplication.haveSetAlertList();
  }

  /**
   * Send a metagame event subscription message for every server provided.
   * TODO: Check that subscribing to a server multiple times doesn't cause multiple events to be sent for a single alert.
   */
  private void sendAlertSubscription(Session session, ServerList servers) {
    String requestString = "{\"service\":\"event\",\"action\":\"subscribe\",\"worlds\":[";

    if (servers.size() == 0) {
      return;
    }

    // Add the servers to track alerts for. Do the last server in the list outside of the loop
    // because of commas.
    for (int i = 0; i < servers.size() - 1; i++) {
      Server server = servers.get(i);
      int serverId = server.getServerId();
      requestString += "\"" + serverId + "\",";
    }
    Server lastServer = servers.get(servers.size() - 1);
    int lastServerId = lastServer.getServerId();
    requestString += "\"" + lastServerId + "\",";
    requestString += "],\"eventNames\":[\"MetagameEvent\"]}";

    // Send metagame subscription message
    try {
      writeToPersistentLog("Sending alert subscription message for: " + requestString, null);
      session.getBasicRemote().sendText(requestString);
    } catch (IOException e) {
      Log.e(TAG, "Error sending alert subscription.", e);
      writeToPersistentLog("Error sending alert subscription.", e);
    }
  }

  private void writeToPersistentLog(String message, Exception exception) {
    if (connectionLogWriter == null) {
      return;
    }

    if (message != null) {
      connectionLogWriter.append(message).append("\n");
    }
    if (exception != null) {
      connectionLogWriter.append(Log.getStackTraceString(exception)).append("\n");
    }

    connectionLogWriter.flush();
  }
}
