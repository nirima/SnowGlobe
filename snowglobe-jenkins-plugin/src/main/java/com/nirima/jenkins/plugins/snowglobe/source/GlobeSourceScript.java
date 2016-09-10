package com.nirima.jenkins.plugins.snowglobe.source;

import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeData;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Created by magnayn on 10/09/2016.
 */
public class GlobeSourceScript extends GlobeSource {
  public String script;

  @DataBoundConstructor
  public GlobeSourceScript(String name, String script) {
    super(name);
    this.script = script;
  }

  @Override
  public SnowGlobeData getData(Run<?, ?> run, FilePath filePath, Launcher launcher,
                               TaskListener taskListener) throws IOException, InterruptedException {
    return new SnowGlobeData(name, script, null);
  }

  @Extension
  public static final class DescriptorImpl extends GlobeSourceDescriptor {
    @Override
    public String getDisplayName() {
      return "Supplied Script";
    }
  }
}
