package org.david.planetside.alertnotifier.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.david.planetside.alertnotifier.AlertNotifierApplication;
import org.david.planetside.alertnotifier.R;
import org.david.planetside.alertnotifier.connection.WebsocketClient;
import org.david.planetside.alertnotifier.controller.ServerAlertListAdapter;

public class MainActivity extends Activity {
  public static final String SHARED_PREFS_FILE = "PS2_AlertNotifier_Prefs_File";
  private static final String TAG = MainActivity.class.getSimpleName();
  private static final String FIRST_RUN_KEY = "FIRST_RUN";
  private static final String APP_VERSION_KEY = "APP_VERSION";
  private static final String APP_VERSION = "Alpha";

  private ServerAlertListAdapter serverAlertListAdapter;

  private BroadcastReceiver alertDataUpdatedBroadcastReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          haveUpdatedDataSet();
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.active_alerts_list);

    Log.d(TAG, "onCreate MainActivity has been called!");

    // Start the websocket client service. It will handle the case where it is already running.
    Intent serviceStartIntent = new Intent(getApplicationContext(), WebsocketClient.class);
    getApplicationContext().startService(serviceStartIntent);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // The first time launching the application a select server fragment should be shown.
    // After that, just display current alerts.
    SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_FILE, 0 /** Private */);
    if (sharedPrefs.getBoolean(FIRST_RUN_KEY, true)) {
      Log.d(TAG, "First time launching application.");
      sharedPrefs.edit().putBoolean(FIRST_RUN_KEY, false).apply();
      sharedPrefs.edit().putString(APP_VERSION_KEY, APP_VERSION).apply();

      startActivity(new Intent(this, SelectServerActivity.class));
    } else {
      Log.d(TAG, "Not the first time running the application.");
      final AlertNotifierApplication alertNotifierApplication =
          (AlertNotifierApplication) getApplication();

      // Display a progress bar while waiting for alerts to be fetched
      View progressBarView = findViewById(R.id.active_alerts_progress_bar);
      progressBarView.setVisibility(View.VISIBLE);

      // TODO: Turn this into a proper asynctask that use doInBackground and postExecute
      AsyncTask.execute(new Runnable() {
        @Override
        public void run() {
          try {
            while (!alertNotifierApplication.hasAlertList()) {
              // TODO: There is a way to do this with a semaphore, but this is called before the service even has a chance to init. So I need to avoid a race here.
              // I also need this to look nice because the adapter operates on a different thread from this wait.
              Thread.sleep(1000);
            }
          } catch (InterruptedException e) {
            Log.w(TAG, "Interrupted while waiting for alerts. Will just display now.");
          }

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              View progressBarView = findViewById(R.id.active_alerts_progress_bar);
              progressBarView.setVisibility(View.GONE);

              if (alertNotifierApplication.getServerAlerts().size() == 0) {
                View noActiveAlertsView = findViewById(R.id.no_active_alerts_notification);
                noActiveAlertsView.setVisibility(View.VISIBLE);
              } else {
                View noActiveAlertsView = findViewById(R.id.no_active_alerts_notification);
                noActiveAlertsView.setVisibility(View.GONE);
              }

              ListView listView = (ListView) findViewById(R.id.active_alerts_listview);
              ServerAlertListAdapter adapter =
                  new ServerAlertListAdapter(
                      getApplicationContext(), alertNotifierApplication.getServerAlerts());
              listView.setAdapter(adapter);
              serverAlertListAdapter = adapter;
            }
          });
        }
      });

      registerAlertUpdatedReceiver();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    LocalBroadcastManager localBroadcastManager =
        LocalBroadcastManager.getInstance(getApplicationContext());
    localBroadcastManager.unregisterReceiver(alertDataUpdatedBroadcastReceiver);
  }

  private void haveUpdatedDataSet() {
    if (serverAlertListAdapter == null) {
      // TODO: Because this gets called before the alert adapter is created on first run. I should do this differently so I don't have this logic.
      return;
    }

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (serverAlertListAdapter.getCount() == 0) {
          View noActiveAlertsView = findViewById(R.id.no_active_alerts_notification);
          noActiveAlertsView.setVisibility(View.VISIBLE);
        } else {
          View noActiveAlertsView = findViewById(R.id.no_active_alerts_notification);
          noActiveAlertsView.setVisibility(View.GONE);
        }

        if (serverAlertListAdapter != null) {
          serverAlertListAdapter.notifyDataSetChanged();
        }
      }
    });
  }

  public void registerAlertUpdatedReceiver() {
    LocalBroadcastManager localBroadcastManager =
        LocalBroadcastManager.getInstance(getApplicationContext());
    localBroadcastManager.registerReceiver(
        alertDataUpdatedBroadcastReceiver, new IntentFilter("ALERT_DATA_UPDATED"));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.server_settings) {
      startActivity(new Intent(this, SelectServerActivity.class));
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
}
