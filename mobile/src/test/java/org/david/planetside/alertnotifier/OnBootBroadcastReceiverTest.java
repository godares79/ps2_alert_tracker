package org.david.planetside.alertnotifier;

import android.content.Intent;

import org.david.planetside.alertnotifier.connection.WebsocketClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.david.planetside.alertnotifier.OnBootBroadcastReceiver}.
 */
@Config(
    manifest = "src/main/AndroidManifest.xml",
    emulateSdk = 16)
@RunWith(RobolectricTestRunner.class)
public class OnBootBroadcastReceiverTest {
  @Test
  public void testReceiverRegistered() {
    List<ShadowApplication.Wrapper> registeredReceivers = ShadowApplication.getInstance().getRegisteredReceivers();
    assertFalse(registeredReceivers.isEmpty());

    boolean foundReceiver = false;
    for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
      foundReceiver = OnBootBroadcastReceiver.class.getSimpleName().equals(
          wrapper.broadcastReceiver.getClass().getSimpleName());

      if (foundReceiver) {
        break;
      }
    }

    assertTrue(foundReceiver);
  }

  @Test
  public void testRegisteredForBootCompletedAction() {
    Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
    assertTrue(ShadowApplication.getInstance().hasReceiverForIntent(intent));
  }

  @Test
  public void testOnlyOneReceiverRegistered() {
    Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
    assertEquals(1, ShadowApplication.getInstance().getReceiversForIntent(intent).size());
  }

  @Test
  public void testSendOnBootBroadcast() {
    Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
    RuntimeEnvironment.application.sendBroadcast(intent);
    assertEquals(
        WebsocketClient.class.getCanonicalName(),
        ShadowApplication.getInstance().getNextStartedService().getComponent().getClassName());
  }
}
