package org.david.planetside.alertnotifier;

import android.app.Application;

import org.david.planetside.alertnotifier.connection.RESTClient;
import org.david.planetside.alertnotifier.model.ContinentList;
import org.david.planetside.alertnotifier.model.FactionList;
import org.david.planetside.alertnotifier.model.ServerAlert;
import org.david.planetside.alertnotifier.model.ServerList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extend the {@link Application} to provide some global state for things like the list of servers
 * and the list of factions.
 */
public class AlertNotifierApplication extends Application {
  private List<ServerAlert> serverAlerts = new ArrayList<>();
  private final Object serversLock = new Object();
  private ServerList serverList;
  private final Object factionsLock = new Object();
  private FactionList factionList;
  private final Object continentsLock = new Object();
  private ContinentList continentList;
  private boolean hasAlertList;

  public ServerList getServerList(RESTClient restClient) throws IOException {
    synchronized (serversLock) {
      // If the server list doesn't already exist, then initialize it.
      if (serverList == null) {
        initializeServerList(restClient);
      }

      return serverList;
    }
  }

  public void setServerListForTest(ServerList serverList) {
    this.serverList = serverList;
  }

  public FactionList getFactionList(RESTClient restClient) throws IOException {
    synchronized (factionsLock) {
      // If the faction list doesn't already exist, then initialize it.
      if (factionList == null) {
        initializeFactionList(restClient);
      }

      return factionList;
    }
  }

  public ContinentList getContinentList(RESTClient restClient) throws IOException {
    synchronized (continentsLock) {
      // If the continent list does not already exist, initialize it.
      if (continentList == null) {
        initializeContinentList(restClient);
      }

      return continentList;
    }
  }

  private void initializeServerList(RESTClient restClient) throws IOException {
    serverList = restClient.getServers();
  }

  private void initializeFactionList(RESTClient restClient) throws IOException {
    factionList = restClient.getFactions(getApplicationContext());
  }

  private void initializeContinentList(RESTClient restClient) throws IOException {
    continentList = restClient.getContinents();
  }

  public List<ServerAlert> getServerAlerts() {
    return serverAlerts;
  }

  public void haveSetAlertList() {
    hasAlertList = true;
  }

  public boolean hasAlertList() {
    return hasAlertList;
  }

  public void addServerAlert(ServerAlert serverAlert) {
    if (!serverAlerts.contains(serverAlert)) {
      serverAlerts.add(serverAlert);
    }
  }
}
