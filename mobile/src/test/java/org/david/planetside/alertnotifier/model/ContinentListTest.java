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
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link ContinentList}.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ContinentListTest {

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullContinentList() throws JSONException {
    ContinentList continentList = ContinentList.parse(null);
    assertNull(continentList);
  }

  @Test
  public void testParse() throws IOException, JSONException {
    ContinentList continentList = getContinentListForTest();
    assertNotNull(continentList);
  }

  @Test
  public void testGetContinentWithId_validId() throws IOException, JSONException {
    ContinentList continentList = getContinentListForTest();
    assertNotNull(continentList);

    int continentId = 2;
    Continent continent = continentList.getContinentWithId(continentId);
    assertEquals(continentId, continent.getId());

    continentId = 4;
    continent = continentList.getContinentWithId(continentId);
    assertEquals(continentId, continent.getId());

    continentId = 6;
    continent = continentList.getContinentWithId(continentId);
    assertEquals(continentId, continent.getId());

    continentId = 8;
    continent = continentList.getContinentWithId(continentId);
    assertEquals(continentId, continent.getId());
  }

  @Test
  public void testGetContinentWithId_invalidId() throws IOException, JSONException {
    ContinentList continentList = getContinentListForTest();
    assertNotNull(continentList);

    int continentId = -1;
    Continent continent = continentList.getContinentWithId(continentId);
    assertNull(continent);

    continentId = 15;
    continent = continentList.getContinentWithId(continentId);
    assertNull(continent);
  }

  @Test
  public void testGetContinentForMetagameEventId_validId() throws IOException, JSONException {
    ContinentList continentList = getContinentListForTest();
    assertNotNull(continentList);

    // There is a mapping between metagame event ids and continent ids.
    int metagameEventId = 1;
    int continentId = 2;
    Continent continent = continentList.getContinentForMetagameEventId(metagameEventId);
    assertEquals(continentId, continent.getId());

    metagameEventId = 2;
    continentId = 8;
    continent = continentList.getContinentForMetagameEventId(metagameEventId);
    assertEquals(continentId, continent.getId());

    metagameEventId = 3;
    continentId = 6;
    continent = continentList.getContinentForMetagameEventId(metagameEventId);
    assertEquals(continentId, continent.getId());

    metagameEventId = 4;
    continentId = 4;
    continent = continentList.getContinentForMetagameEventId(metagameEventId);
    assertEquals(continentId, continent.getId());
  }

  @Test
  public void testGetContinentForMetagameEventId_invalidId() throws IOException, JSONException {
    ContinentList continentList = getContinentListForTest();
    assertNotNull(continentList);

    int metagameEventId = -1;
    Continent continent = continentList.getContinentForMetagameEventId(metagameEventId);
    assertNull(continent);

    metagameEventId = 10;
    continent = continentList.getContinentForMetagameEventId(metagameEventId);
    assertNull(continent);
  }

  private ContinentList getContinentListForTest() throws IOException, JSONException {
    return ContinentList.parse(new JSONObject(TestData.openAndReadLine(this.getClass(), 0)));
  }
}
