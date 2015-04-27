package org.david.planetside.alertnotifier.model;

import org.david.planetside.alertnotifier.TestData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * Tests for {@link FactionList}.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class FactionListTest {

  FactionList factionList;

  @Before
  public void setUp() throws IOException, JSONException {
    factionList = FactionList.parse(getFactionListData(), RuntimeEnvironment.application);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullFactionList() throws Exception {
    FactionList.parse(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullContext() throws Exception {
    FactionList.parse(getFactionListData(), null);
  }

  @Test
  public void testParse() {
    assertEquals(3, factionList.size());
    for (Faction faction : factionList) {
      assertNotNull(faction);
      assertNotNull(faction.getIcon());
      assertNotNull(faction.getName());
      assertNotSame(0, faction.getId());
    }
  }

  @Test
  public void testGetFactionWithId_validId() {
    int factionId = 1;
    Faction faction = factionList.getFactionWithId(factionId);
    assertNotNull(faction);
    assertEquals(factionId, faction.getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetFactionWithId_invalidId() {
    factionList.getFactionWithId(-1);
  }

  private JSONObject getFactionListData() throws IOException, JSONException {
    return new JSONObject(TestData.openAndReadLine(this.getClass(), 0));
  }
}
