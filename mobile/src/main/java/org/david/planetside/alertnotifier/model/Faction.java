package org.david.planetside.alertnotifier.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.david.planetside.alertnotifier.R;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A faction.
 */
public class Faction {
  private final int id;
  private final String name;
  private final Bitmap icon;

  public Faction(int id, String name, Bitmap icon) {
    this.id = id;
    this.name = name;
    this.icon = icon;
  }

  public static Faction parse(JSONObject factionObject, Context context) throws JSONException {
    if (factionObject == null) {
      throw new IllegalArgumentException("The factionObject is null!");
    } else if (context == null) {
      throw new IllegalArgumentException("The context is null!");
    }

    int id = Integer.valueOf(factionObject.getString("faction_id"));
    String name = factionObject.getJSONObject("name").getString("en");
    boolean userSelectable = factionObject.getString("user_selectable").equals("1");

    // Do not include non-playable races.
    if (userSelectable) {
      // Hardcode the bitmap for now because the images from the server are high resolution.
      // Change this later if the faction information returned from census includes a small image.
      int iconId = 0;
      if (id == 1) {  // VS
        iconId = R.drawable.vs;
      } else if (id == 2) {  // NC
        iconId = R.drawable.nc;
      } else if (id == 3) {  // TR
        iconId = R.drawable.tr;
      }
      Bitmap factionIcon = BitmapFactory.decodeResource(context.getResources(), iconId);

      return new Faction(id, name, factionIcon);
    }

    return null;
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

  public Bitmap getIcon() {
    return icon;
  }
}
