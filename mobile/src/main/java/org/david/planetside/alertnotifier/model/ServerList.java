package org.david.planetside.alertnotifier.model;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * The list of all servers.
 */
public class ServerList extends ArrayList<Server> {
  private static final String TAG = ServerList.class.getSimpleName();
  private static final String SERVER_LIST_KEY = "SERVER_LIST";

  public static ServerList parse(JSONObject servers) throws JSONException {
    if (servers == null) {
      throw new IllegalArgumentException("The servers object is null!");
    }

    ServerList serverList = new ServerList();
    JSONArray serverListJSON = servers.getJSONArray("world_list");

    for (int index = 0; index < serverListJSON.length(); index++) {
      serverList.add(Server.parse(serverListJSON.getJSONObject(index)));
    }

    return serverList;
  }

  /**
   * Read the list of servers from shared preferences.
   * List will be empty if there are no shared preferences.
   */
  public static ServerList getSavedServerList(SharedPreferences sharedPreferences) {
    ServerList serverList = new ServerList();

    String serverListString = sharedPreferences.getString(SERVER_LIST_KEY, null);
    Log.i(TAG, "Getting saved server list.");
    if (serverListString != null) {
      try {
        JSONArray serverListJSON = new JSONArray(serverListString);
        Log.d(TAG, "Saved server list is: " + serverListJSON);
        for (int index = 0; index < serverListJSON.length(); index++) {
          serverList.add(Server.parse(serverListJSON.getJSONObject(index)));
        }
      } catch (JSONException e) {
        Log.e(TAG, "Error reading saved server list.", e);
      }
    }

    return serverList;
  }

  // TODO: Give a better name.
  public Server from(int id) {
    for (Server server : this) {
      if (server.getServerId() == id) {
        return server;
      }
    }

    return null;
  }

  /**
   * Save the list of servers to shared preferences.
   */
  public void saveServerList(SharedPreferences sharedPreferences) throws JSONException {
    JSONArray serverListJSON = new JSONArray();
    for (Server server : this) {
      Log.d(TAG, "Adding a saved server: " + this.toString());
      // Use the same format as the server list returned from the census server.
      JSONObject object = new JSONObject();
      object.put("name", new JSONObject().put("en", server.getServerName()));
      object.put("world_id", server.getServerId());
      serverListJSON.put(object);
    }

    sharedPreferences.edit().putString(SERVER_LIST_KEY, serverListJSON.toString()).apply();
  }

  @Override
  public String toString() {
    String serverListString = "";
    for (Server server : this) {
      serverListString += "(" + server.toString() + ")";
    }
    return serverListString;
  }
}
