package com.nirima.jenkins.plugins.snowglobe.action;

import java.io.Serializable;

import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;

/**
 * Created by magnayn on 09/09/2016.
 */
public class RunLink implements Serializable {
  final int runId;
  final String projectName;

  public RunLink(Run<?,?> run) {
    this.runId = run.getNumber();
    this.projectName = run.getParent().getName();
  }

  protected Run<?,?> getRun() {
    Project
        item = ((Project) Jenkins.getInstance().getItem(projectName));

    if( item == null )
      throw new IllegalStateException("Project " + projectName + " no longer exists");

    return item.getBuildByNumber(runId);
  }
}
