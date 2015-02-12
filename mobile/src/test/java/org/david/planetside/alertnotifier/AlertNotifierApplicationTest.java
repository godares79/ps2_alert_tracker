package org.david.planetside.alertnotifier;

import android.content.Context;

import org.david.planetside.alertnotifier.connection.RESTClient;
import org.david.planetside.alertnotifier.model.ContinentList;
import org.david.planetside.alertnotifier.model.FactionList;
import org.david.planetside.alertnotifier.model.ServerAlert;
import org.david.planetside.alertnotifier.model.ServerList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AlertNotifierApplication}.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AlertNotifierApplicationTest {
  AlertNotifierApplication alertNotifierApplication;

  @Mock private RESTClient restClient;
  @Mock private ServerList serverList;
  @Mock private FactionList factionList;
  @Mock private ContinentList continentList;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);

    alertNotifierApplication = new AlertNotifierApplication();
    doReturn(serverList).when(restClient).getServers();
    doReturn(factionList).when(restClient).getFactions(isA(Context.class));
    doReturn(continentList).when(restClient).getContinents();
  }

  @Test
  public void testGetServerList_alreadyInitialized() throws IOException {
    // Init by calling once
    alertNotifierApplication.getServerList(restClient);

    // Just call get server list several times
    ServerList serverList = alertNotifierApplication.getServerList(restClient);
    assertEquals(this.serverList, serverList);
    serverList = alertNotifierApplication.getServerList(restClient);
    assertEquals(this.serverList, serverList);
    verify(restClient, times(1)).getServers();  // Confirm client called only once
  }

  @Test
  public void testGetServerList_notInitialized() throws IOException {
    ServerList serverList = alertNotifierApplication.getServerList(restClient);
    verify(restClient, times(1)).getServers();
    assertEquals(this.serverList, serverList);
  }

  @Test
  public void testGetServerList_notInitialized_multipleThreads()
      throws IOException, InterruptedException {
    final Semaphore s = new Semaphore(-1);
    Runnable getServerListRunnable = new Runnable() {
      @Override
      public void run() {
        try {
          ServerList serverList1 = alertNotifierApplication.getServerList(restClient);
          assertEquals(serverList, serverList1);
        } catch (IOException e) {
          fail("Should not receive an IOException.");
        }

        s.release();
      }
    };

    Thread callThread1 = new Thread(getServerListRunnable);
    Thread callThread2 = new Thread(getServerListRunnable);
    callThread1.start();
    callThread2.start();
    s.acquire();

    verify(restClient, times(1)).getServers();
  }

  @Test
  public void testGetFactionList_alreadyInitialized() throws IOException {
    // Init by calling once
    alertNotifierApplication.getFactionList(restClient);

    // Call getFactionList several times
    FactionList factionList = alertNotifierApplication.getFactionList(restClient);
    assertEquals(this.factionList, factionList);
    factionList = alertNotifierApplication.getFactionList(restClient);
    assertEquals(this.factionList, factionList);
    verify(restClient, times(1)).getFactions(isA(Context.class));
  }

  @Test
  public void testGetFactionList_notInitialized() throws IOException {
    FactionList factionList = alertNotifierApplication.getFactionList(restClient);
    verify(restClient, times(1)).getFactions(isA(Context.class));
    assertEquals(this.factionList, factionList);
  }

  @Test
  public void testGetFactionList_notInitialized_multipleThreads()
      throws IOException, InterruptedException {
    final Semaphore s = new Semaphore(-1);
    Runnable getFactionListRunnable = new Runnable() {
      @Override
      public void run() {
        try {
          FactionList factionList1 = alertNotifierApplication.getFactionList(restClient);
          assertEquals(factionList, factionList1);
        } catch (IOException e) {
          fail("Should not receive an IOException.");
        }

        s.release();
      }
    };

    Thread callThread1 = new Thread(getFactionListRunnable);
    Thread callThread2 = new Thread(getFactionListRunnable);
    callThread1.start();
    callThread2.start();
    s.acquire();

    verify(restClient, times(1)).getFactions(isA(Context.class));
  }

  @Test
  public void testGetContinentList_alreadyInitialized() throws IOException {
    // Init by calling once
    alertNotifierApplication.getContinentList(restClient);

    // Call getFactionList several times
    ContinentList continentList = alertNotifierApplication.getContinentList(restClient);
    assertEquals(this.continentList, continentList);
    continentList = alertNotifierApplication.getContinentList(restClient);
    assertEquals(this.continentList, continentList);
    verify(restClient, times(1)).getContinents();
  }

  @Test
  public void testGetContinentList_notInitialized() throws IOException {
    ContinentList continentList = alertNotifierApplication.getContinentList(restClient);
    verify(restClient, times(1)).getContinents();
    assertEquals(this.continentList, continentList);
  }

  @Test
  public void testGetContinentList_notInitialized_multipleThreads()
      throws IOException, InterruptedException {
    final Semaphore s = new Semaphore(-1);
    Runnable getContinentListRunnable = new Runnable() {
      @Override
      public void run() {
        try {
          ContinentList continentList1 = alertNotifierApplication.getContinentList(restClient);
          assertEquals(continentList, continentList1);
        } catch (IOException e) {
          fail("Should not receive an IOException.");
        }

        s.release();
      }
    };

    Thread callThread1 = new Thread(getContinentListRunnable);
    Thread callThread2 = new Thread(getContinentListRunnable);
    callThread1.start();
    callThread2.start();
    s.acquire();

    verify(restClient, times(1)).getContinents();
  }

  @Test
  public void testAddServerAlert() {
    ServerAlert serverAlert = mock(ServerAlert.class);
    when(serverAlert.getInstanceId()).thenReturn(1);

    // Try adding a server alert multiple times and ensure it still works.
    alertNotifierApplication.addServerAlert(serverAlert);
    assertEquals(alertNotifierApplication.getServerAlerts().size(), 1);
    assertEquals(alertNotifierApplication.getServerAlerts().get(0), serverAlert);
    alertNotifierApplication.addServerAlert(serverAlert);
    assertEquals(alertNotifierApplication.getServerAlerts().size(), 1);
    assertEquals(alertNotifierApplication.getServerAlerts().get(0), serverAlert);

  }
}
