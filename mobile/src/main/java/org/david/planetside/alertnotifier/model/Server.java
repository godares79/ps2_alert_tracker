package org.david.planetside.alertnotifier.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Server and corresponding server id.
 */
public class Server {
  private final int serverId;
  private final String serverName;

  public Server(int serverId, String serverName) {
    this.serverName = serverName;
    this.serverId = serverId;
  }

  public static Server parse(JSONObject server) throws JSONException {
    if (server == null) {
      throw new IllegalArgumentException("The server object is null!");
    }
    
    int id = Integer.valueOf(server.getString("world_id"));
    String name = server.getJSONObject("name").getString("en");

    return new Server(id, name);
  }

  @Override
  public String toString() {
    return String.format("%s (%d)", getServerName(), getServerId());
  }

  public String getServerName() {
    return serverName;
  }

  public int getServerId() {
    return serverId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Server) {
      if (serverId == ((Server) obj).getServerId()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int hashCode() {
    // Because each server has a unique server ID this is actually pretty good hash. But it's
    // not needed because I'm not using hash-based storage for servers. It's just a best practice
    // to override hashCode if overriding equals.
    return serverId;
  }
}
