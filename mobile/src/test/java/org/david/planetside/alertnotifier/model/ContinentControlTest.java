package org.david.planetside.alertnotifier.model;

import org.david.planetside.alertnotifier.TestData;
import org.json.JSONArray;
import org.json.JSONException;
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
 * Tests for {@link ContinentControl}.
 * <p>Test data is an array with 10 regions. Faction ID's are 1, 2 and 3.
 * <p>Control for ID 1: 30, ID 2: 40, ID 3: 30.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ContinentControlTest {

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullObject() throws JSONException {
    ContinentControl.parse(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullFactionList() throws IOException, JSONException {
    ContinentControl.parse(getContinentControlJSONArrayForTest(), null);
  }

  @Test
  public void testParse_factionListDoesNotContainAllFactions() throws IOException, JSONException {
    JSONArray continentControlArray = getContinentControlJSONArrayForTest();
    FactionList factionList = mock(FactionList.class);
    Faction mockFaction = mock(Faction.class);
    when(factionList.getFactionWithId(1)).thenReturn(mockFaction);

    ContinentControl continentControl = ContinentControl.parse(continentControlArray, factionList);
    assertNotNull(continentControl);
    assertEquals(mockFaction, continentControl.getFactionControlList().get(0).getFaction());
    assertEquals(30, continentControl.getFactionControlList().get(0).getControl());
  }

  @Test
  public void testParse() throws JSONException, IOException {
    // Create a JSON object out of the test data.
    JSONArray continentControlArray = getContinentControlJSONArrayForTest();
    FactionList factionList = mock(FactionList.class);
    Faction mockFaction1 = mock(Faction.class);
    Faction mockFaction2 = mock(Faction.class);
    Faction mockFaction3 = mock(Faction.class);
    when(factionList.getFactionWithId(1)).thenReturn(mockFaction1);
    when(factionList.getFactionWithId(2)).thenReturn(mockFaction2);
    when(factionList.getFactionWithId(3)).thenReturn(mockFaction3);

    // Parse and check that the expected ContinentControl object was returned.
    ContinentControl continentControl = ContinentControl.parse(continentControlArray, factionList);
    assertNotNull(continentControl);
    assertEquals(mockFaction1, continentControl.getFactionControlList().get(0).getFaction());
    assertEquals(30, continentControl.getFactionControlList().get(0).getControl());
    assertEquals(mockFaction2, continentControl.getFactionControlList().get(1).getFaction());
    assertEquals(40, continentControl.getFactionControlList().get(1).getControl());
    assertEquals(mockFaction3, continentControl.getFactionControlList().get(2).getFaction());
    assertEquals(30, continentControl.getFactionControlList().get(2).getControl());
  }

  private JSONArray getContinentControlJSONArrayForTest() throws IOException, JSONException {
    return new JSONArray(TestData.openAndReadLine(this.getClass(), 0));
  }
}
