package org.david.planetside.alertnotifier.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Population of a server.
 */
// TODO: Can combine this and FactionPopulation.
public class ServerPopulation {
  private final List<FactionPopulation> factionPopulationList;

  public ServerPopulation(List<FactionPopulation> factionPopulationList) {
    this.factionPopulationList = factionPopulationList;
  }

  public static ServerPopulation parse(JSONObject object, FactionList factionList) throws JSONException {
    List<FactionPopulation> factionPopulationList = new ArrayList<FactionPopulation>();
    factionPopulationList.add(FactionPopulation.parse(object.getJSONObject("vs"), factionList));
    factionPopulationList.add(FactionPopulation.parse(object.getJSONObject("nc"), factionList));
    factionPopulationList.add(FactionPopulation.parse(object.getJSONObject("tr"), factionList));

    return new ServerPopulation(factionPopulationList);
  }

  public static ServerPopulation unknownPopulation() {
    return new ServerPopulation(new ArrayList<FactionPopulation>());
  }

  @Override
  public String toString() {
    return getFactionPopulationList().toString();
  }

  public List<FactionPopulation> getFactionPopulationList() {
    return factionPopulationList;
  }
}
