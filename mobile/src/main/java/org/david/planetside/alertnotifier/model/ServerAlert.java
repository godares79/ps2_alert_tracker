package org.david.planetside.alertnotifier.model;

import android.util.Log;

import org.david.planetside.alertnotifier.connection.RESTClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The alert information for a server.
 */
public class ServerAlert {
  public static final long ALERT_LENGTH_MS = 7200000;
  private static final String TAG = ServerAlert.class.getSimpleName();
  private Server server;
  private ContinentControl continentControl;
  private ServerPopulation serverPopulation;
  private Continent continent;
  private Date alertStartTime;
  private boolean active;
  private int experienceBonus;
  private int instanceId;

  /**
   * Parse a server alert from the provided JSONObject.
   * <p/>
   * If the metagame_event_state_name is marked as "started" then there is currently an
   * alert in progress. If not, then there are no current alerts.
   * Also note, there are multiple metagame_event_id's (some metagame events are not continent
   * conquering alerts). The keep things simple for now, just handle continent locking alerts.
   * Continent locking alerts will have an ID of: 1 (Indar), 2 (Esamir), 3 (Amerish), 4 (Hossin).
   */
  public static ServerAlert parseFromRestResponse(
      JSONObject alertObject, ServerList serverList, ContinentList continentList, FactionList factionList, RESTClient restClient)
      throws JSONException {
    assertParametersValid(alertObject, serverList, continentList, factionList, restClient);

    Log.i(TAG, alertObject.toString(2));

    ServerAlert alert = new ServerAlert();

    Server alertServer = serverList.from(alertObject.getInt("world_id"));
    alert.setServer(alertServer);

    alert.setExperienceBonus(alertObject.getInt("experience_bonus"));
    alert.setInstanceId(alertObject.getInt("instance_id"));

    String eventState = alertObject.getString("metagame_event_state_name");
    if (eventState.equals("started")) {
      alert.setActive(true);
    }

    int alertTypeId = alertObject.getInt("metagame_event_id");
    alert.setContinent(continentList.getContinentForMetagameEventId(alertTypeId));

    alert.setAlertStartTime(new Date(alertObject.getLong("timestamp") * 1000));

    try {
      alert.setContinentControl(restClient.getContinentControl(alertServer, alert.getContinent().getId(), factionList));
    } catch (IOException e) {
      Log.e(TAG, "Problem retrieving continent control.", e);
      alert.setContinentControl(null);
    }

    try {
      alert.setServerPopulation(restClient.getPopulation(alertServer, factionList));
    } catch (IOException e) {
      Log.e(TAG, "Problem retrieving server population.", e);
      alert.setServerPopulation(null);
    }

    return alert;
  }

  /**
   * The JSON format of the websocket payload is slightly different from the REST client response.
   * So parse it in a different method.
   */
  public static ServerAlert parseFromWebsocketPayload(
      JSONObject alertObject, ServerList serverList, ContinentList continentList, FactionList factionList, RESTClient restClient)
      throws JSONException {
    assertParametersValid(alertObject, serverList, continentList, factionList, restClient);

    JSONObject intermediateObject = alertObject.getJSONObject("payload");
    return parseFromRestResponse(intermediateObject, serverList, continentList, factionList, restClient);
  }

  private static void assertParametersValid(
      JSONObject alertObject, ServerList serverList, ContinentList continentList, FactionList factionList, RESTClient restClient) {
    if (alertObject == null) {
      throw new IllegalArgumentException("The alertObject is null!");
    } else if (serverList == null) {
      throw new IllegalArgumentException("The serverList is null!");
    } else if (continentList == null) {
      throw new IllegalArgumentException("The continentList is null!");
    } else if (factionList == null) {
      throw new IllegalArgumentException("The factionList is null!");
    } else if (restClient == null) {
      throw new IllegalArgumentException("The restClient is null!");
    }
  }

  @Override
  public String toString() {
    String alertString = "";
    alertString += "Server: " + getServer().toString() + "\n";
    alertString += "Continent: " + getContinent().toString() + "\n";
    alertString += "Start Time: " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(getAlertStartTime()) + "\n";
    alertString += "Active: " + isActive() + "\n";
    alertString += "Experience Bonus: " + getExperienceBonus() + "\n";
    alertString += "Instance ID: " + getInstanceId() + "\n";
    if (getContinentControl() != null) {
      alertString += "Continent Control: " + getContinentControl().toString() + "\n";
    } else {
      alertString += "Continent control is null!";
    }
    if (getServerPopulation() != null) {
      alertString += "Server Population: " + getServerPopulation().toString() + "\n";
    } else {
      alertString += "Server population is null!";
    }
    return alertString;
  }

  public Continent getContinent() {
    return continent;
  }

  public void setContinent(Continent continent) {
    this.continent = continent;
  }

  public Date getAlertStartTime() {
    return alertStartTime;
  }

  public void setAlertStartTime(Date alertStartTime) {
    this.alertStartTime = alertStartTime;
  }

  public ContinentControl getContinentControl() {
    return continentControl;
  }

  public void setContinentControl(ContinentControl continentControl) {
    this.continentControl = continentControl;
  }

  public ServerPopulation getServerPopulation() {
    return serverPopulation;
  }

  public void setServerPopulation(ServerPopulation serverPopulation) {
    this.serverPopulation = serverPopulation;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getExperienceBonus() {
    return experienceBonus;
  }

  public void setExperienceBonus(int experienceBonus) {
    this.experienceBonus = experienceBonus;
  }

  public int getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(int instanceId) {
    this.instanceId = instanceId;
  }

  public Server getServer() {
    return server;
  }

  public void setServer(Server server) {
    this.server = server;
  }

  @Override
  public boolean equals(Object candidate) {
    if (candidate instanceof ServerAlert) {
      ServerAlert candidateServerAlert = (ServerAlert) candidate;
      if (getInstanceId() == candidateServerAlert.getInstanceId()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int hashCode() {
    return getInstanceId();
  }
}
