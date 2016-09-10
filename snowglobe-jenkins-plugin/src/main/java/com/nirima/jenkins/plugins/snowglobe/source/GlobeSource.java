package com.nirima.jenkins.plugins.snowglobe.source;

import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeData;

import java.io.IOException;
import java.io.Serializable;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;

/**
 * Created by magnayn on 10/09/2016.
 */
public  abstract class GlobeSource implements Describable<GlobeSource>, Serializable {

  public final String name;

  public GlobeSource(String name) {
    this.name = name;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Descriptor<GlobeSource> getDescriptor() {
    return Jenkins.getInstance().getDescriptorOrDie(getClass());
  }

  public abstract SnowGlobeData getData(Run<?, ?> run, FilePath filePath, Launcher launcher,
                               TaskListener taskListener) throws IOException, InterruptedException;
}
