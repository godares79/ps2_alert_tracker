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
 * Tests for {@link Continent}.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ContinentTest {

  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullObject() throws JSONException {
    Continent continent = Continent.parse(null);
    assertNull(continent);
  }

  @Test
  public void testParse() throws JSONException, IOException {
    Continent continent = Continent.parse(
        new JSONObject(TestData.openAndReadLine(this.getClass(), 0)));

    assertNotNull(continent);
    assertEquals(continent.getId(), 2);
    assertEquals(continent.getName(), "Indar");
  }
}
