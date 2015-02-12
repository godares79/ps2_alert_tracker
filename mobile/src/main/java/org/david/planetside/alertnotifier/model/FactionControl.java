package org.david.planetside.alertnotifier.model;

/**
 * The territory control of a faction (either over a continent or server or other).
 */
// TODO: Can combine this class and ContinentControl... probably keep this one.
public class FactionControl {
  private final Faction faction;
  private final int control;

  public FactionControl(Faction faction, int control) {
    this.faction = faction;
    this.control = control;
  }

  @Override
  public String toString() {
    return getFaction().toString() + " -- Control: " + getControl();
  }

  public Faction getFaction() {
    return faction;
  }

  public int getControl() {
    return control;
  }
}
