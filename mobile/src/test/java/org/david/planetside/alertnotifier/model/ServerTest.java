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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link Server}.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ServerTest {
  @Test(expected = IllegalArgumentException.class)
  public void testParse_nullServerObject() throws JSONException {
    Server.parse(null);
  }

  @Test
  public void testParse() throws IOException, JSONException {
    Server server = Server.parse(getServerObjectData());
    assertEquals(13, server.getServerId());
    assertEquals("Cobalt", server.getServerName());
  }

  @Test
  public void testEquals() {
    Server left = new Server(1, "ONE");
    Server right = new Server(1, "NOT ONE");
    Server notEqual = new Server(2, "TWO");

    assertTrue(left.equals(right));
    assertTrue(right.equals(left));
    assertFalse(left.equals(notEqual));
    assertFalse(right.equals(notEqual));
  }

  @Test
  public void testHashCode() {
    Server server = new Server(1, "ONE");
    assertEquals(1, server.hashCode());
  }

  private JSONObject getServerObjectData() throws IOException, JSONException {
    return new JSONObject(TestData.openAndReadLine(this.getClass(), 0));
  }
}
