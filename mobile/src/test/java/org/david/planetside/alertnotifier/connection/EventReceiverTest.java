package org.david.planetside.alertnotifier.connection;

import org.david.planetside.alertnotifier.AlertNotifierApplication;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import static org.mockito.Mockito.mock;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class EventReceiverTest {

  private EventReceiver eventReceiver;

  @Before
  public void setUp() {
    eventReceiver = new EventReceiver((AlertNotifierApplication) RuntimeEnvironment.application, null);
  }

  @Ignore("Does not work. See comments in test method for explanation.")
  @Test
  public void testOnOpen() {
    // Get an exception when trying to mock Session. But actually, it's not good practice
    // to mock out objects that you don't own. Your test will break if the library changes.
    // Instead, what you usually need to do is create a wrapper of the library (which you should
    // be doing in your application anyways) and then mock that out. But I can't do that here
    // because I'm working with an override.
    // My best option is to create a test version of a class that implements the Session interface.
    // That is a decent amount of work, so I will leave it for now. There is more value is writing
    // unit tests for my data classes behaviour.
    Session mockSession = mock(Session.class);
    EndpointConfig mockEndpointConfig = null;  // not used

    eventReceiver.onOpen(mockSession, mockEndpointConfig);
  }
}
