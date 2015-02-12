package org.david.planetside.alertnotifier.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A list of all factions.
 */
public class FactionList extends ArrayList<Faction> {
  public static FactionList parse(JSONObject factionsObject, Context context) throws JSONException {
    if (factionsObject == null) {
      throw new IllegalArgumentException("The factionsObject is null!");
    } else if (context == null) {
      throw new IllegalArgumentException("The context is null!");
    }
    
    FactionList factionList = new FactionList();
    JSONArray factionListArray = factionsObject.getJSONArray("faction_list");

    for (int index = 0; index < factionListArray.length(); index++) {
      Faction faction = Faction.parse(factionListArray.getJSONObject(index), context);

      if (faction != null) {
        factionList.add(faction);
      }
    }

    return factionList;
  }

  @Override
  public String toString() {
    String factionListString = "";
    for (Faction faction : this) {
      factionListString += "(" + faction.toString() + ")";
    }
    return factionListString;
  }

  public Faction getFactionWithId(int id) {
    for (Faction faction : this) {
      if (faction.getId() == id) {
        return faction;
      }
    }

    throw new IllegalArgumentException("No Faction with id: " + id);
  }
}
