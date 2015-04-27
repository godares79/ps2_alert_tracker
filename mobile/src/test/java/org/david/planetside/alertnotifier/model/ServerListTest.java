package org.david.planetside.alertnotifier.model;

import android.content.SharedPreferences;

import org.david.planetside.alertnotifier.TestData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link ServerList}.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ServerListTest {
  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullServerList() throws JSONException {
    ServerList.parse(null);
  }

  @Test
  public void testParse() throws IOException, JSONException {
    ServerList serverList = ServerList.parse(getServerListData());
    assertEquals(6, serverList.size());
  }

  @Test
  public void getSavedServerList_noSavedServers() {
    SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences("UNUSED", 0);

    ServerList serverList = ServerList.getSavedServerList(sharedPreferences);
    assertEquals(0, serverList.size());
  }

  @Test
  public void getSavedServerList_hasSavedServers() throws JSONException {
    SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences("UNUSED", 0);
    ServerList serverList = new ServerList();
    Server one = new Server(1, "ONE");
    serverList.add(one);
    Server two = new Server(2, "TWO");
    serverList.add(two);
    Server three = new Server(3, "THREE");
    serverList.add(three);
    serverList.saveServerList(sharedPreferences);

    ServerList savedServerList = ServerList.getSavedServerList(sharedPreferences);
    assertEquals(3, savedServerList.size());
    assertArrayEquals(serverList.toArray(), savedServerList.toArray());
  }

  @Test
  public void testFrom_invalidId() throws IOException, JSONException {
    ServerList serverList = ServerList.parse(getServerListData());
    assertNull(serverList.from(-1));
    assertNull(serverList.from(100));
  }

  @Test
  public void testFrom_validId() throws IOException, JSONException {
    ServerList serverList = ServerList.parse(getServerListData());
    assertNotNull(serverList.from(25));
    assertNotNull(serverList.from(13));
    assertNotNull(serverList.from(17));
  }

  @Test
  public void testSaveServerList_noServersToSave() throws JSONException {
    ServerList serverList = new ServerList();
    SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences("UNUSED", 0);
    serverList.saveServerList(sharedPreferences);
    assertEquals(0, ServerList.getSavedServerList(sharedPreferences).size());
  }

  private JSONObject getServerListData() throws IOException, JSONException {
    return new JSONObject(TestData.openAndReadLine(this.getClass(), 0));
  }
}
