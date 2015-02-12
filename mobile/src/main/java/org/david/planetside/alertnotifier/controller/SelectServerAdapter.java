package org.david.planetside.alertnotifier.controller;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import org.david.planetside.alertnotifier.model.Server;
import org.david.planetside.alertnotifier.model.ServerList;

/**
 * Adapter for setting the list of servers in the server list layout.
 */
public class SelectServerAdapter extends BaseAdapter {
  private final Context context;
  private ServerList allServers;
  private ServerList trackedServers;

  public SelectServerAdapter(Context context, ServerList allServers, ServerList trackedServers) {
    this.context = context;
    this.allServers = allServers;
    this.trackedServers = trackedServers;
  }

  @Override
  public int getCount() {
    return allServers.size();
  }

  @Override
  public Object getItem(int i) {
    return allServers.get(i);
  }

  @Override
  public long getItemId(int i) {
    return allServers.get(i).getServerId();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // Don't worry about using convertView. This won't get large enough to scroll.
    Server server = allServers.get(position);
    CheckBox checkBox = new CheckBox(context);
    checkBox.setText(server.getServerName());

    if (trackedServers.contains(server)) {
      checkBox.setChecked(true);
    }

    return checkBox;
  }

  public void updateAllServers(ServerList newAllServers) {
    allServers = newAllServers;
  }
}
