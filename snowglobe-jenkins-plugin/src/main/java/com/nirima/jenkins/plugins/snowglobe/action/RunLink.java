package com.nirima.jenkins.plugins.snowglobe.action;

import java.io.Serializable;

import hudson.model.Project;
import hudson.model.Run;
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
    return ((Project) Jenkins.getInstance().getItem(projectName)).getBuildByNumber(runId);
  }
}
