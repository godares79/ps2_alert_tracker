package org.david.planetside.alertnotifier.ui;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import org.david.planetside.alertnotifier.AlertNotifierApplication;
import org.david.planetside.alertnotifier.R;
import org.david.planetside.alertnotifier.model.Server;
import org.david.planetside.alertnotifier.model.ServerList;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link SelectServerActivity}.
 */
@Config(
    manifest = "src/main/AndroidManifest.xml",
    emulateSdk = 16)
@RunWith(RobolectricTestRunner.class)

public class SelectServerActivityTest {
  private SelectServerActivity selectServerActivity;

  private ServerList allServers;
  private ServerList trackedServers;
  private Server server1, server2, server3, server4;

  @Before
  public void setUp() throws JSONException {
    server1 = new Server(1, "Server1");
    server2 = new Server(2, "Server2");
    server3 = new Server(3, "Server3");
    server4 = new Server(4, "Server4");

    // 4 servers total
    allServers = new ServerList();
    allServers.add(server1);
    allServers.add(server2);
    allServers.add(server3);
    allServers.add(server4);
    ((AlertNotifierApplication) Robolectric.application).setServerListForTest(allServers);

    // 2 servers are being tracked already
    trackedServers = new ServerList();
    trackedServers.add(server1);
    trackedServers.add(server2);
    trackedServers.saveServerList(
        Robolectric.application.getSharedPreferences(
            MainActivity.SHARED_PREFS_FILE, 0 /* private */));

    selectServerActivity = Robolectric.buildActivity(SelectServerActivity.class).setup().get();
  }

  @Test
  public void testCorrectBoxesCheckedGivenStoredServerData() {
    ListView listView = (ListView) selectServerActivity.findViewById(R.id.server_list);
    assertEquals(4, listView.getCount());

    for (int i = 0; i < listView.getCount(); i++) {
      CheckBox listItem = (CheckBox) listView.getChildAt(i);
      Server serverForItem = (Server) listView.getItemAtPosition(i);

      assertEquals(serverForItem.getServerName(), listItem.getText());
      if (trackedServers.contains(serverForItem)) {
        assertTrue(listItem.isChecked());
      }
    }
  }

  @Test
  public void testStartTrackingServer() {
    ListView listView = (ListView) selectServerActivity.findViewById(R.id.server_list);

    // Check the box for server3. Need to do it in this looping way because there is no way
    // to directly get the view for a given item.
    for (int i = 0; i < listView.getCount(); i++) {
      CheckBox listItem = (CheckBox) listView.getChildAt(i);
      Server serverForItem = (Server) listView.getItemAtPosition(i);

      if (serverForItem.equals(server3)) {
        listItem.setChecked(true);
      }
    }

    // Press the doneButton
    Button doneButton = (Button) selectServerActivity.findViewById(R.id.done_server_select_button);
    doneButton.performClick();

    // Confirm that three servers are now being tracked: server1, server2, and server3
    ServerList newlyTrackedServers = ServerList.getSavedServerList(
        Robolectric.application.getSharedPreferences(
            MainActivity.SHARED_PREFS_FILE, 0 /* private */));
    assertEquals(3, newlyTrackedServers.size());
    assertTrue(newlyTrackedServers.contains(server1));
    assertTrue(newlyTrackedServers.contains(server2));
    assertTrue(newlyTrackedServers.contains(server3));

    // Confirm that an local intent was sent for updating the server subscriptions
    ShadowLocalBroadcastManager shadowLocalBroadcastManager =
        Robolectric.shadowOf(LocalBroadcastManager.getInstance(Robolectric.application));
    List<Intent> sentIntents = shadowLocalBroadcastManager.getSentBroadcastIntents();
    assertEquals(1, sentIntents.size());
    assertEquals("ALERT_SUBSCRIPTION_UPDATED", sentIntents.get(0).getAction());
  }

  @Test
  public void testStopTrackingServer() {
    ListView listView = (ListView) selectServerActivity.findViewById(R.id.server_list);

    // Uncheck the box for server2. Need to do it in this looping way because there is no way
    // to directly get the view for a given item.
    for (int i = 0; i < listView.getCount(); i++) {
      CheckBox listItem = (CheckBox) listView.getChildAt(i);
      Server serverForItem = (Server) listView.getItemAtPosition(i);

      if (serverForItem.equals(server2)) {
        listItem.setChecked(false);
      }
    }

    // Press the doneButton
    Button doneButton = (Button) selectServerActivity.findViewById(R.id.done_server_select_button);
    doneButton.performClick();

    // Confirm that three servers are now being tracked: server1, server2, and server3
    ServerList newlyTrackedServers = ServerList.getSavedServerList(
        Robolectric.application.getSharedPreferences(
            MainActivity.SHARED_PREFS_FILE, 0 /* private */));
    assertEquals(1, newlyTrackedServers.size());
    assertTrue(newlyTrackedServers.contains(server1));

    // Confirm that an local intent was sent for updating the server subscriptions
    ShadowLocalBroadcastManager shadowLocalBroadcastManager =
        Robolectric.shadowOf(LocalBroadcastManager.getInstance(Robolectric.application));
    List<Intent> sentIntents = shadowLocalBroadcastManager.getSentBroadcastIntents();
    assertEquals(1, sentIntents.size());
    assertEquals("ALERT_SUBSCRIPTION_UPDATED", sentIntents.get(0).getAction());
  }

  @Test
  public void testServerListFetchingTimeout() {

  }
}
