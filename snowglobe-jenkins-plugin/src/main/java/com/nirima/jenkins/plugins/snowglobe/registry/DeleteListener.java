package com.nirima.jenkins.plugins.snowglobe.registry;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.listeners.RunListener;

/**
 * Created by magnayn on 12/09/2016.
 */
@Extension
public class DeleteListener extends RunListener<Run> {
  @Override
  public void onDeleted(Run r) {
    SnowGlobeRegistry.get().removeFromRun(r);
  }
}