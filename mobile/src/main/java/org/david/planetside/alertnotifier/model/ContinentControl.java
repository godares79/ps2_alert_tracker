package org.david.planetside.alertnotifier.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The faction control of an arbitrary continent or server.
 */
public class ContinentControl {
  private static final String TAG = ContinentControl.class.getSimpleName();
  private final List<FactionControl> factionControlList;

  public ContinentControl(List<FactionControl> factionControlList) {
    this.factionControlList = factionControlList;
  }

  public static ContinentControl parse(JSONArray continentControlArray, FactionList factionList) throws JSONException {
    if (continentControlArray == null) {
      throw new IllegalArgumentException("The continentControlArray is null!");
    } else if (factionList == null) {
      throw new IllegalArgumentException("The factionList is null!");
    }

    // TODO: Make the key be a Faction object rather than faction id
    Map<Integer, Integer> controlMap = new HashMap<Integer, Integer>();

    int regionCount = continentControlArray.length();
    for (int i = 0; i < regionCount; i++) {
      JSONObject regionControl = continentControlArray.getJSONObject(i);
      int regionControlFactionId = new Integer(regionControl.getJSONObject("RowData").getString("FactionId"));
      Log.i(TAG, "Have a faction control region of: " + regionControlFactionId);
      if (controlMap.containsKey(regionControlFactionId)) {
        int controlCount = controlMap.get(regionControlFactionId) + 1;
        Log.i(TAG, "Updating faction control for region " + regionControlFactionId + " to: " + controlCount);
        controlMap.put(regionControlFactionId, controlCount);
      } else {
        Log.i(TAG, "Setting faction control for region " + regionControlFactionId + "to 1.");
        controlMap.put(regionControlFactionId, 1);
      }
    }

    for (Map.Entry<Integer, Integer> entry : controlMap.entrySet()) {
      int key = entry.getKey();
      Log.i(TAG, "The number of regions is: " + regionCount);
      Log.i(TAG, "The value for faction id " + key + " is " + entry.getValue());
      int value = Math.round((entry.getValue() * 100) / regionCount);
      Log.i(TAG, "Setting control value of " + value + " for faction id " + key);
      controlMap.put(key, value);
    }

    // TODO: Make this generic. Confirm the ID values are actually correct.
    List<FactionControl> factionControlList = new ArrayList<FactionControl>();
    if (controlMap.containsKey(1)) {
      factionControlList.add(
          new FactionControl(factionList.getFactionWithId(1), controlMap.get(1)));
    }
    if (controlMap.containsKey(2)) {
      factionControlList.add(
          new FactionControl(factionList.getFactionWithId(2), controlMap.get(2)));
    }
    if (controlMap.containsKey(3)) {
      factionControlList.add(
          new FactionControl(factionList.getFactionWithId(3), controlMap.get(3)));
    }

    return new ContinentControl(factionControlList);
  }

  @Override
  public String toString() {
    String controlString = "";
    for (FactionControl factionControl : factionControlList) {
      controlString += "(" + factionControl.toString() + ") ";
    }
    return controlString;
  }

  public List<FactionControl> getFactionControlList() {
    return factionControlList;
  }
}
