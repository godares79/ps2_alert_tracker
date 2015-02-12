package org.david.planetside.alertnotifier.model;

import org.david.planetside.alertnotifier.TestData;
import org.david.planetside.alertnotifier.connection.RESTClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServerAlert}.
 * Data file contains several alerts.
 * Line 0: REST response alert. Completed.
 * Line 1: REST response alert. Currently active.
 * Line 2: Websocket alert payload. Starting.
 * Line 3: Websocket alert payload. Ending.
 * Line 4: not a server alert notification.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ServerAlertTest {

  @Mock private ServerList serverList;
  @Mock private ContinentList continentList;
  @Mock private FactionList factionList;
  @Mock private RESTClient restClient;
  @Mock private Server server;
  @Mock private Continent continent;
  @Mock private ContinentControl continentControl;
  @Mock private ServerPopulation serverPopulation;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseFromRestResponse_nullServerAlert() throws JSONException {
    ServerAlert.parseFromRestResponse(null, null, null, null, null);
  }

  @Test
  public void testParseFromRestResponse_nullSupportingObjects() throws IOException, JSONException {
    // Handle a bunch of scenarios in this test method.
    JSONObject serverAlertObject = getServerAlertData(0);

    try {
      ServerAlert.parseFromRestResponse(serverAlertObject, null, null, null, null);
      fail("Should not be able to pass null server list!");
    } catch (IllegalArgumentException e) {
      // Ignore.
    }

    try {
      ServerAlert.parseFromRestResponse(serverAlertObject, serverList, null, null, null);
      fail("Should not be able to pass null continent list!");
    } catch (IllegalArgumentException e) {
      // Ignore.
    }

    try {
      ServerAlert.parseFromRestResponse(serverAlertObject, serverList, continentList, null, null);
      fail("Should not be able to pass null faction list!");
    } catch (IllegalArgumentException e) {
      // Ignore.
    }

    try {
      ServerAlert.parseFromRestResponse(serverAlertObject, serverList, continentList, factionList, null);
      fail("Should not be able to pass null REST client!");
    } catch (IllegalArgumentException e) {
      // Ignore.
    }
  }

  @Test(expected = JSONException.class)
  public void testParseFromRestResponse_notServerAlert() throws IOException, JSONException {
    JSONObject badServerAlertObject = getServerAlertData(4);
    ServerAlert.parseFromRestResponse(badServerAlertObject, serverList, continentList, factionList, restClient);
  }

  @Test
  public void testParseFromRestResponse_activeAlert() throws IOException, JSONException {
    JSONObject serverAlertObject = getServerAlertData(1);
    when(serverList.from(13)).thenReturn(server);
    when(continentList.getContinentForMetagameEventId(4)).thenReturn(continent);
    when(continent.getId()).thenReturn(1);
    when(restClient.getContinentControl(server, 1, factionList)).thenReturn(continentControl);
    when(restClient.getPopulation(server, factionList)).thenReturn(serverPopulation);

    ServerAlert serverAlert = ServerAlert.parseFromRestResponse(
        serverAlertObject, serverList, continentList, factionList, restClient);
    assertEquals(server, serverAlert.getServer());
    assertEquals(continent, serverAlert.getContinent());
    assertTrue(serverAlert.isActive());
    assertEquals(30, serverAlert.getExperienceBonus());
    assertEquals(4685, serverAlert.getInstanceId());
    assertEquals(continentControl, serverAlert.getContinentControl());
    assertEquals(serverPopulation, serverAlert.getServerPopulation());
    assertEquals((long) 1422858268 * 1000, serverAlert.getAlertStartTime().getTime());
  }

  @Test
  public void testParseFromRestResponse_endedAlert() throws IOException, JSONException {
    JSONObject serverAlertObject = getServerAlertData(0);
    when(serverList.from(10)).thenReturn(server);
    when(continentList.getContinentForMetagameEventId(1)).thenReturn(continent);
    when(continent.getId()).thenReturn(1);
    when(restClient.getContinentControl(server, 1, factionList)).thenReturn(continentControl);
    when(restClient.getPopulation(server, factionList)).thenReturn(serverPopulation);

    ServerAlert serverAlert = ServerAlert.parseFromRestResponse(
        serverAlertObject, serverList, continentList, factionList, restClient);
    assertEquals(server, serverAlert.getServer());
    assertEquals(continent, serverAlert.getContinent());
    assertFalse(serverAlert.isActive());
    assertEquals(30, serverAlert.getExperienceBonus());
    assertEquals(4640, serverAlert.getInstanceId());
    assertEquals(continentControl, serverAlert.getContinentControl());
    assertEquals(serverPopulation, serverAlert.getServerPopulation());
    assertEquals((long) 1422858694 * 1000, serverAlert.getAlertStartTime().getTime());
  }

  @Test
  public void testParseFromWebsocketPayload() throws IOException, JSONException {
    JSONObject serverAlertObject = getServerAlertData(2);
    when(serverList.from(25)).thenReturn(server);
    when(continentList.getContinentForMetagameEventId(3)).thenReturn(continent);
    when(continent.getId()).thenReturn(1);
    when(restClient.getContinentControl(server, 1, factionList)).thenReturn(continentControl);
    when(restClient.getPopulation(server, factionList)).thenReturn(serverPopulation);

    ServerAlert serverAlert = ServerAlert.parseFromWebsocketPayload(
        serverAlertObject, serverList, continentList, factionList, restClient);
    assertEquals(server, serverAlert.getServer());
    assertEquals(continent, serverAlert.getContinent());
    assertTrue(serverAlert.isActive());
    assertEquals(30, serverAlert.getExperienceBonus());
    assertEquals(1822, serverAlert.getInstanceId());
    assertEquals(continentControl, serverAlert.getContinentControl());
    assertEquals(serverPopulation, serverAlert.getServerPopulation());
    assertEquals((long) 1422859325 * 1000, serverAlert.getAlertStartTime().getTime());
  }

  @Test
  public void testEquals() {
    ServerAlert serverAlert1 = new ServerAlert();
    ServerAlert serverAlert2 = new ServerAlert();

    serverAlert1.setInstanceId(1);
    assertTrue(serverAlert1.equals(serverAlert1));
    assertFalse(serverAlert1.equals(null));
    assertFalse(serverAlert1.equals(serverAlert2));

    serverAlert2.setInstanceId(1);
    assertTrue(serverAlert1.equals(serverAlert2));
    assertTrue(serverAlert2.equals(serverAlert1));

    serverAlert2.setInstanceId(2);
    assertFalse(serverAlert1.equals(serverAlert2));
    assertFalse(serverAlert2.equals(serverAlert1));
  }

  @Test
  public void testHashCode() {
    ServerAlert serverAlert = new ServerAlert();
    serverAlert.setInstanceId(1);

    assertEquals(1, serverAlert.hashCode());
  }

  private JSONObject getServerAlertData(int lineNumber) throws IOException, JSONException {
    return new JSONObject(TestData.openAndReadLine(this.getClass(), lineNumber));
  }
}
