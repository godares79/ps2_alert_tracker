package org.david.planetside.alertnotifier.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A continent in the world.
 */
public class Continent {
  private final int id;
  private final String name;

  public Continent(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public static Continent parse(JSONObject continentObject) throws JSONException {
    if (continentObject == null) {
      throw new IllegalArgumentException("The continentObject is null!");
    }

    int id = new Integer(continentObject.getString("zone_id"));
    String name = continentObject.getJSONObject("name").getString("en");

    return new Continent(id, name);
  }

  @Override
  public String toString() {
    return String.format("%s (%d)", getName(), getId());
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
