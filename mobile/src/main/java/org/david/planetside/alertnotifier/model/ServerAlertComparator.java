package org.david.planetside.alertnotifier.model;

import java.util.Comparator;
import java.util.Date;

/**
 * Comparator for ServerAlerts. Sorts them in ascending order of time left in the alert.
 */
public class ServerAlertComparator implements Comparator<ServerAlert>  {
  @Override
  public int compare(ServerAlert lhs, ServerAlert rhs) {
    Date lhsTimeRemaining = ServerAlert.getFinishTime(lhs.getAlertStartTime());
    Date rhsTimeRemaining = ServerAlert.getFinishTime(rhs.getAlertStartTime());

    return lhsTimeRemaining.compareTo(rhsTimeRemaining);
  }

  @Override
  public boolean equals(Object object) {
    return (this == object);
  }
}
