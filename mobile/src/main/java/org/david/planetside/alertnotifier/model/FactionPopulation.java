package org.david.planetside.alertnotifier.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The population of a faction (either for a server or continent or other).
 */
public class FactionPopulation {
  private final Faction faction;
  private final int population;

  public FactionPopulation(Faction faction, int population) {
    this.faction = faction;
    this.population = population;
  }

  public static FactionPopulation parse(JSONObject factionPopulation, FactionList factionList) throws JSONException {
    if (factionPopulation == null) {
      throw new IllegalArgumentException("The factionPopluation is null!");
    } else if (factionList == null) {
      throw new IllegalArgumentException("The factionList is null!");
    }

    int factionId = factionPopulation.getInt("faction_id");
    int populationPercentage =
        Integer.valueOf(factionPopulation.getString("population_percentage").replace("%", ""));

    return new FactionPopulation(factionList.getFactionWithId(factionId), populationPercentage);
  }

  @Override
  public String toString() {
    return getFaction().toString() + " -- Population: " + getPopulation();
  }

  public Faction getFaction() {
    return faction;
  }

  public int getPopulation() {
    return population;
  }
}
