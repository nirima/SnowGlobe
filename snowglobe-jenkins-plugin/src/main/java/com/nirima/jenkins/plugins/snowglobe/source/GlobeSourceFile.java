package com.nirima.jenkins.plugins.snowglobe.source;

import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeAction;
import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeData;

import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOUtils;
import jenkins.MasterToSlaveFileCallable;

/**
 * Created by magnayn on 10/09/2016.
 */
public class GlobeSourceFile extends GlobeSource {
    public final String file;

  @DataBoundConstructor
  public GlobeSourceFile(String name, String file) {
    super(name);
    this.file = file;
  }

  @Override
  public SnowGlobeData getData(Run<?, ?> run, FilePath filePath, Launcher launcher,
                               TaskListener taskListener) throws IOException, InterruptedException {
    return filePath.act(new MasterToSlaveFileCallable<SnowGlobeData>() {
      @Override
      public SnowGlobeData invoke(File file, VirtualChannel virtualChannel)
          throws IOException, InterruptedException {

        return new SnowGlobeData(name, FileUtils.readFileToString(file), null);
      }
    });
  }

  @Extension
  public static final class DescriptorImpl extends GlobeSourceDescriptor {
    @Override
    public String getDisplayName() {
      return "Source file in workspace";
    }
  }
}
