package org.david.planetside.alertnotifier.model;

import org.david.planetside.alertnotifier.TestData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FactionPopulation}.
 * There is one faction in the provided data. Population percentages are:
 * ID 1: 38
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class FactionPopulationTest {

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullFactionPopulation() throws JSONException {
    FactionPopulation.parse(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullFactionList() throws JSONException, IOException {
    FactionPopulation.parse(getFactionPopulationObject(), null);
  }

  @Test
  public void testParse() throws IOException, JSONException {
    FactionList factionList = mock(FactionList.class);
    Faction faction = mock(Faction.class);
    when(factionList.getFactionWithId(1)).thenReturn(faction);

    FactionPopulation factionPopulation = FactionPopulation.parse(getFactionPopulationObject(), factionList);
    assertNotNull(factionPopulation);
    assertEquals(faction, factionPopulation.getFaction());
    assertEquals(38, factionPopulation.getPopulation());
  }

  private JSONObject getFactionPopulationObject() throws IOException, JSONException {
    return new JSONObject(TestData.openAndReadLine(this.getClass(), 0));
  }
}
