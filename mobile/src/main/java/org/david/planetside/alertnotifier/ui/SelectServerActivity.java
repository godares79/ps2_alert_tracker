package org.david.planetside.alertnotifier.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.david.planetside.alertnotifier.AlertNotifierApplication;
import org.david.planetside.alertnotifier.R;
import org.david.planetside.alertnotifier.connection.RESTClient;
import org.david.planetside.alertnotifier.controller.SelectServerAdapter;
import org.david.planetside.alertnotifier.model.Server;
import org.david.planetside.alertnotifier.model.ServerList;
import org.json.JSONException;

import java.io.IOException;

/**
 * Displays the servers and allows selecting one or more.
 */
public class SelectServerActivity extends Activity {
  private static final String TAG = SelectServerActivity.class.getSimpleName();

  private ProgressBar progressBar;
  private SelectServerAdapter selectServerAdapter;
  private Button doneButton;
  private ListView listView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.select_server_layout);

    // Set up the loading screen
    progressBar = (ProgressBar) findViewById(R.id.select_server_progress_bar);
    progressBar.setVisibility(View.VISIBLE);

    // Set up the server list adapter
    listView = (ListView) findViewById(R.id.server_list);
    ServerList trackedServers = ServerList.getSavedServerList(
        getSharedPreferences(MainActivity.SHARED_PREFS_FILE, 0 /** private */));
    selectServerAdapter = new SelectServerAdapter(
        getApplicationContext(), new ServerList(), trackedServers);
    listView.setAdapter(selectServerAdapter);

    // Download the list of servers
    new DownloadServerListTask().execute();

    // Set up the Done button click listener
    doneButton = (Button) findViewById(R.id.done_server_select_button);
    doneButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "The done button has been pressed.");

        ServerList newTrackedServers = new ServerList();
        for (int index = 0; index < selectServerAdapter.getCount(); index++) {
          Server server = (Server) selectServerAdapter.getItem(index);
          CheckBox serverView = (CheckBox) listView.getChildAt(index);

          if (server != null && serverView != null && serverView.isChecked()) {
            Log.i(TAG, "Adding server to tracked server list = " + server.getServerId() + ":" + server.getServerName());
            newTrackedServers.add(server);
          }
        }

        try {
          newTrackedServers.saveServerList(
              getSharedPreferences(MainActivity.SHARED_PREFS_FILE, 0 /** private */));
        } catch (JSONException e) {
          Log.e(TAG, "Error outputting tracked server list to shared preferences.", e);
        }

        // Update the alert subscription in the service.
        LocalBroadcastManager localBroadcastManager =
            LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(new Intent("ALERT_SUBSCRIPTION_UPDATED"));

        finish();
      }
    });
  }

  private class DownloadServerListTask extends AsyncTask<Void, Void, ServerList> {
    @Override
    protected ServerList doInBackground(Void... voids) {
      RESTClient restClient = new RESTClient();
      try {
        return ((AlertNotifierApplication) getApplication()).getServerList(restClient);
      } catch (IOException e) {
        Log.e(TAG, "Problem retrieving list of servers.", e);
      }

      return null;
    }

    protected void onPostExecute(ServerList serverList) {
      if (progressBar != null) {
        progressBar.setVisibility(View.GONE);
      }

      doneButton.setVisibility(View.VISIBLE);
      selectServerAdapter.updateAllServers(serverList);
      selectServerAdapter.notifyDataSetChanged();
    }
  }
}
