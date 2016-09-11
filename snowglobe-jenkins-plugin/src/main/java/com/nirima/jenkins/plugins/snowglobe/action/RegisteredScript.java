package com.nirima.jenkins.plugins.snowglobe.action;

import java.io.IOException;
import java.io.Serializable;

import hudson.model.Run;

/**
 * Created by magnayn on 09/09/2016.
 */
public class RegisteredScript implements Serializable {
  public final String script;
  public final String id;

  public RegisteredScript(String script, String id) {
    this.script = script;
    this.id = id;
  }

  public SnowGlobeData build(Run<?,?> run) throws IOException {
    SnowGlobeAction action = run.getAction(SnowGlobeAction.class);
    if( action==null )
      action = new SnowGlobeAction(run);

    SnowGlobeData data = action.createSnowglobe(this);

    run.save();
    return data;
  }

}
