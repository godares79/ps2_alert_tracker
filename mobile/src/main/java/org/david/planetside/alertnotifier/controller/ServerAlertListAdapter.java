package org.david.planetside.alertnotifier.controller;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.david.planetside.alertnotifier.R;
import org.david.planetside.alertnotifier.model.ContinentControl;
import org.david.planetside.alertnotifier.model.FactionControl;
import org.david.planetside.alertnotifier.model.FactionPopulation;
import org.david.planetside.alertnotifier.model.ServerAlert;
import org.david.planetside.alertnotifier.model.ServerPopulation;

import java.util.Date;
import java.util.List;

public class ServerAlertListAdapter extends BaseAdapter {
  private static final String TAG = ServerAlertListAdapter.class.getSimpleName();
  private final Context context;
  private final List<ServerAlert> alertList;

  public ServerAlertListAdapter(
      Context context, List<ServerAlert> alertList) {
    this.context = context;
    this.alertList = alertList;
  }

  @Override
  public int getCount() {
    return alertList.size();
  }

  @Override
  public Object getItem(int i) {
    return alertList.get(i);
  }

  @Override
  public long getItemId(int i) {
    // TODO: Can make this better if I actually need it.
    return alertList.get(i).hashCode();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Log.i(TAG, "getView has been called with position: " + position);

    // Do not add a row if there are no active alerts associated with the tracked server.
    final ServerAlert serverAlert = alertList.get(position);
    // TODO: Handle a null server alert... or the case where alertList does not have an item at that position.

    // Use the old view if possible
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView;
    if (convertView != null) {
      Log.d(TAG, "The old view is not null!");
      rowView = convertView;
    } else {
      Log.d(TAG, "The old view is null!");
      rowView = inflater.inflate(R.layout.alert_list_item, parent, false);
    }

    View factionControlBlock = rowView.findViewById(R.id.alert_faction_control);
    View factionPopulationBlock = rowView.findViewById(R.id.alert_faction_population);

    // Build the server alert row
    TextView serverTextView = (TextView) rowView.findViewById(R.id.alert_item_server);
    serverTextView.setText(serverAlert.getServer().getServerName());

    TextView continentTextView = (TextView) rowView.findViewById(R.id.alert_item_continent);
    continentTextView.setText(serverAlert.getContinent().getName());

    ContinentControl continentControl = serverAlert.getContinentControl();
    ViewGroup factionControlView =
        (ViewGroup) factionControlBlock.findViewById(R.id.alert_faction_control_data);
    // TODO: Get rid this removeAllViews and properly avoid reinflating if I don't need to
    factionControlView.removeAllViews();
    for (FactionControl faction : continentControl.getFactionControlList()) {
      View factionPairView = inflater.inflate(R.layout.faction_pair, factionControlView, false);

      ImageView iconView = (ImageView) factionPairView.findViewById(R.id.icon);
      TextView valueView = (TextView) factionPairView.findViewById(R.id.value);
      iconView.setImageBitmap(faction.getFaction().getIcon());
      valueView.setText(faction.getControl() + "%");
      factionControlView.addView(factionPairView);
    }

    ServerPopulation serverPopulation = serverAlert.getServerPopulation();
    ViewGroup factionPopulationView =
        (ViewGroup) factionPopulationBlock.findViewById(R.id.alert_faction_population_data);
    // TODO: Get rid this removeAllViews and properly avoid reinflating if I don't need to
    factionPopulationView.removeAllViews();
    for (FactionPopulation faction : serverPopulation.getFactionPopulationList()) {
      View factionPairView = inflater.inflate(R.layout.faction_pair, factionPopulationView, false);

      ImageView iconView = (ImageView) factionPairView.findViewById(R.id.icon);
      TextView valueView = (TextView) factionPairView.findViewById(R.id.value);
      iconView.setImageBitmap(faction.getFaction().getIcon());
      valueView.setText(faction.getPopulation() + "%");
      factionPopulationView.addView(factionPairView);
    }

    // Start the countdown timer
    Date endTime = ServerAlert.getFinishTime(serverAlert.getAlertStartTime());
    long msUntilFinished = ServerAlert.getMsUntilAlertFinished(endTime);
    final TextView countdownView = (TextView) rowView.findViewById(R.id.alert_time_remaining);

    if (rowView.getTag() != null) {
      // Have an already running CountDownTimer. So cancel it.
      CountDownTimer oldCountDownTimer = (CountDownTimer) rowView.getTag();
      oldCountDownTimer.cancel();
    }

    CountDownTimer countDownTimer = new CountDownTimer(msUntilFinished, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
        long secondsUntilFinished = millisUntilFinished / 1000;
        double hours = Math.floor(secondsUntilFinished / (60 * 60));
        double minutes = Math.floor((secondsUntilFinished % (60 * 60)) / 60);
        double seconds = Math.ceil((secondsUntilFinished % (60 * 60)) % 60);

        String timeString = "";
        if (hours == 0) {
          timeString = "0";
        }
        timeString += (int) hours + ":";

        if (minutes < 10) {
          timeString += "0";
        }
        timeString += (int) minutes + ":";

        if (seconds < 10) {
          timeString += "0";
        }
        timeString += (int) seconds;

        countdownView.setText(timeString);
      }

      @Override
      public void onFinish() {
        // Remove the alert from the active alerts list.
        NotificationCreator notificationCreator = new NotificationCreator();
        notificationCreator.cancelNotification(context, serverAlert.getServer().getServerId());
        alertList.remove(serverAlert);

        LocalBroadcastManager localBroadcastManager =
            LocalBroadcastManager.getInstance(context.getApplicationContext());
        localBroadcastManager.sendBroadcast(new Intent("ALERT_DATA_UPDATED"));
      }
    };
    countDownTimer.start();

    // Store the countdowntimer object as a tag on the view so that it can be reused.
    rowView.setTag(countDownTimer);

    return rowView;
  }
}
