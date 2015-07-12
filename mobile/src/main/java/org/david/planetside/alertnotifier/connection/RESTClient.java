package org.david.planetside.alertnotifier.connection;

import android.content.Context;
import android.util.Log;

import org.david.planetside.alertnotifier.model.ContinentControl;
import org.david.planetside.alertnotifier.model.ContinentList;
import org.david.planetside.alertnotifier.model.FactionList;
import org.david.planetside.alertnotifier.model.Server;
import org.david.planetside.alertnotifier.model.ServerAlert;
import org.david.planetside.alertnotifier.model.ServerList;
import org.david.planetside.alertnotifier.model.ServerPopulation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple rest client for communication with server.
 */
// TODO: Use Volley.
public class RESTClient {
  private static final String TAG = RESTClient.class.getSimpleName();

  private static final String ALERT_INFO_QUERY =
      "https://census.soe.com/s:ps2alertnotificationapp/get/ps2:v2/world_event/?type=METAGAME&world_id=%d&c:limit=1";
  private static final String POPULATION_INFO_QUERY =
      "http://api.therebelscum.net/PlanetSide/server_status/?world_id=%d";
  private static final String CONTINENT_CONTROL_QUERY =
      "http://census.soe.com/s:ps2alertnotificationapp/get/ps2:v2/map/?world_id=%d&zone_ids=%d";
  private static final String FACTION_QUERY =
      "http://census.soe.com/s:ps2alertnotificationapp/get/ps2:v2/faction?c:limit=10";
  private static final String CONTINENT_QUERY =
      "http://census.soe.com/s:ps2alertnotificationapp/get/ps2:v2/zone/?c:limit=10";
  private static final String SERVER_QUERY =
      "http://census.soe.com/s:ps2alertnotificationapp/get/ps2:v2/world?c:limit=10";

  /**
   * Get the most recent alert for the given Server.
   * Blocking.
   */
  public ServerAlert getServerAlert(
      Server server, ServerList serverList, ContinentList continentList, FactionList factionList)
      throws IOException {
    JSONObject object =
        getServerResponse(String.format(ALERT_INFO_QUERY, server.getServerId()));

    try {
      JSONArray alertArray = object.getJSONArray("world_event_list");

      if (alertArray.length() == 0) {
        Log.w(TAG, "JSON metagame information returned from server was empty!");
        return null;
      }

      JSONObject alertObject = alertArray.getJSONObject(0);
      return ServerAlert.parseFromRestResponse(alertObject, serverList, continentList, factionList, this);
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing JSON object for the query: " + ALERT_INFO_QUERY, e);
      return null;
    }
  }

  /**
   * Get the current faction population for the server.
   * Blocking.
   */
  public ServerPopulation getPopulation(Server server, FactionList factionList) throws IOException {
    JSONObject object =
        getServerResponse(String.format(POPULATION_INFO_QUERY, server.getServerId()));

    try {
      JSONObject populationObject = object.getJSONObject("population");
      return ServerPopulation.parse(populationObject, factionList);
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing JSON object for the query: " + POPULATION_INFO_QUERY, e);
      return ServerPopulation.unknownPopulation();
    }
  }

  /**
   * Get the current faction control of the given continent.
   * Blocking.
   */
  public ContinentControl getContinentControl(Server server, int continent, FactionList factionList)
      throws IOException {
    JSONObject object =
        getServerResponse(String.format(CONTINENT_CONTROL_QUERY, server.getServerId(), continent));

    try {
      JSONArray mapList = object.getJSONArray("map_list");
      JSONObject regionObject = mapList.getJSONObject(0);
      JSONArray factionControlList = regionObject.getJSONObject("Regions").getJSONArray("Row");

      return ContinentControl.parse(factionControlList, factionList);
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing JSON object for the query: " + CONTINENT_CONTROL_QUERY, e);
      return null;
    }
  }

  /**
   * Returns a mapping of faction to faction ID.
   * Blocking.
   * <p/>
   * Requires an application context in order to load the faction icon drawables.
   */
  public FactionList getFactions(Context context) throws IOException {
    JSONObject object = getServerResponse(FACTION_QUERY);

    try {
      return FactionList.parse(object, context);
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing JSON object for the query: " + FACTION_QUERY, e);
      return null;
    }
  }

  /**
   * Returns a mapping continent ID to continent.
   * Blocking.
   */
  public ContinentList getContinents() throws IOException {
    JSONObject object = getServerResponse(CONTINENT_QUERY);

    try {
      return ContinentList.parse(object);
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing JSON object for query: " + CONTINENT_QUERY, e);
      return null;
    }
  }

  /**
   * Returns a list of all servers.
   * Blocking.
   */
  public ServerList getServers() throws IOException {
    JSONObject object = getServerResponse(SERVER_QUERY);

    try {
      return ServerList.parse(object);
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing JSON object for query: " + SERVER_QUERY, e);
      return null;
    }
  }

  /**
   * Send the provided query to the server and get the response.
   */
  private JSONObject getServerResponse(String query) throws IOException {
    URL url = new URL(query);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Accept", "application/json");

    if (connection.getResponseCode() != 200) {
      Log.e(TAG, "Got a bad http response from server: " + connection.getResponseCode());
      return null;
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
    }

    try {
      return new JSONObject(stringBuilder.toString());
    } catch (JSONException e) {
      Log.e(TAG, "Error parsing JSON from server for the query: " + query, e);
    } finally {
      connection.disconnect();
      reader.close();
    }

    return null;
  }
}
