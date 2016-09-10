package com.nirima.jenkins.plugins.snowglobe.source;

import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeAction;
import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeData;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Created by magnayn on 10/09/2016.
 */
public class GlobeSourceExisting extends GlobeSource {

  @DataBoundConstructor
  public GlobeSourceExisting(String name) {
    super(name);
  }

  @Override
  public SnowGlobeData getData(Run<?, ?> run, FilePath filePath, Launcher launcher,
                               TaskListener taskListener) {
    return run.getAction(SnowGlobeAction.class).getDataById(name);
  }


  @Extension
  public static final class DescriptorImpl extends GlobeSourceDescriptor {
    @Override
    public String getDisplayName() {
      return "Previously created SnowGlobe";
    }
  }
}
