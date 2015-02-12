package org.david.planetside.alertnotifier.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A list of all continents.
 */
public class ContinentList extends ArrayList<Continent> {
  public static ContinentList parse(JSONObject continentsObject) throws JSONException {
    if (continentsObject == null) {
      throw new IllegalArgumentException("The continentsObject is null!");
    }

    ContinentList continentList = new ContinentList();
    JSONArray continentsArrayJSON = continentsObject.getJSONArray("zone_list");

    for (int index = 0; index < continentsArrayJSON.length(); index++) {
      Continent continent = Continent.parse(continentsArrayJSON.getJSONObject(index));

      // Do not include the VR training continents.
      if (!continent.getName().contains("Training")) {
        continentList.add(continent);
      }
    }

    return continentList;
  }

  @Override
  public String toString() {
    String continentListString = "";
    for (Continent continent : this) {
      continentListString += "(" + continent.toString() + ")";
    }
    return continentListString;
  }

  public Continent getContinentWithId(int id) {
    for (Continent continent : this) {
      if (continent.getId() == id) {
        return continent;
      }
    }

    return null;
  }

  /**
   * Maps continent ids to metagame event ids. The metagame event ids are used to indicate which
   * continent is experiencing a metagame event but the mapping seems arbitrary.
   */
  public Continent getContinentForMetagameEventId(int id) {
    switch (id) {
      case 1:
        return getContinentWithId(2);
      case 2:
        return getContinentWithId(8);
      case 3:
        return getContinentWithId(6);
      case 4:
        return getContinentWithId(4);
      default:
        return null;
    }
  }
}
