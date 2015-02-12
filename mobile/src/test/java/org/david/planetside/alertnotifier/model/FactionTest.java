package org.david.planetside.alertnotifier.model;

import org.david.planetside.alertnotifier.TestData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link Faction}.
 * There are two factions in the data file.
 * Line 0: Terran Republic -- user selectable
 * Line 1: Nanite Systems -- not user selectable
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class FactionTest {
  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullFactionObject() throws JSONException {
    Faction.parse(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullContext() throws IOException, JSONException {
    Faction.parse(getFactionObjectData(), null);
  }

  @Test
  public void testParse_userSelectableFaction() throws IOException, JSONException {
    Faction faction = Faction.parse(getFactionObjectData(0), Robolectric.application);
    assertNotNull(faction);
    assertNotNull(faction.getIcon());
    assertNotNull(faction.getName());
    assertNotSame(0, faction.getId());
  }

  @Test
  public void testParse_notUserSelectableFaction() throws IOException, JSONException {
    Faction faction = Faction.parse(getFactionObjectData(1), Robolectric.application);
    assertNull(faction);
  }

  private JSONObject getFactionObjectData() throws IOException, JSONException {
    return getFactionObjectData(0);
  }

  private JSONObject getFactionObjectData(int lineNumber) throws IOException, JSONException {
    return new JSONObject(TestData.openAndReadLine(this.getClass(), lineNumber));
  }
}
