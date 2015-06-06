package org.david.planetside.alertnotifier.model;

import android.util.Log;
import android.util.SparseIntArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    SparseIntArray controlMap = new SparseIntArray();

    int regionCount = continentControlArray.length();
    for (int i = 0; i < regionCount; i++) {
      JSONObject regionControl = continentControlArray.getJSONObject(i);
      int regionControlFactionId = Integer.valueOf(regionControl.getJSONObject("RowData").getString("FactionId"));
      Log.i(TAG, "Have a faction control region of: " + regionControlFactionId);
      if (controlMap.indexOfKey(regionControlFactionId) >= 0) {
        int controlCount = controlMap.get(regionControlFactionId) + 1;
        Log.i(TAG, "Updating faction control for region " + regionControlFactionId + " to: " + controlCount);
        controlMap.put(regionControlFactionId, controlCount);
      } else {
        Log.i(TAG, "Setting faction control for region " + regionControlFactionId + "to 1.");
        controlMap.put(regionControlFactionId, 1);
      }
    }

    for (int index = 0; index < controlMap.size(); index++) {
      int key = controlMap.keyAt(index);
      Log.i(TAG, "The number of regions is: " + regionCount);
      Log.i(TAG, "The value for faction id " + key + " is " + controlMap.get(key));
      int value = Math.round((controlMap.get(key) * 100) / regionCount);
      Log.i(TAG, "Setting control value of " + value + " for faction id " + key);
      controlMap.put(key, value);
    }

    // TODO: Make this generic. Confirm the ID values are actually correct.
    List<FactionControl> factionControlList = new ArrayList<FactionControl>();
    if (controlMap.indexOfKey(1) >= 0) {
      factionControlList.add(
          new FactionControl(factionList.getFactionWithId(1), controlMap.get(1)));
    }
    if (controlMap.indexOfKey(2) >= 0) {
      factionControlList.add(
          new FactionControl(factionList.getFactionWithId(2), controlMap.get(2)));
    }
    if (controlMap.indexOfKey(3) >= 0) {
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
