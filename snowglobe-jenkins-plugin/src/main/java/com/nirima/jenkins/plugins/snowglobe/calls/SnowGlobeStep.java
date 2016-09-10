package com.nirima.jenkins.plugins.snowglobe.calls;

import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeAction;
import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeData;
import com.nirima.jenkins.plugins.snowglobe.source.GlobeSource;
import com.nirima.jenkins.plugins.snowglobe.source.GlobeSourceDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nonnull;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;

/**
 * Created by magnayn on 10/09/2016.
 */
public class SnowGlobeStep extends Builder implements Serializable, SimpleBuildStep {
    public final GlobeSource source;
    public final String action; // create, apply, destroy

  @DataBoundConstructor
  public SnowGlobeStep(GlobeSource source, String action) {
    this.source = source;
    this.action = action;
  }

  @Override
  public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath,
                      @Nonnull Launcher launcher, @Nonnull TaskListener taskListener)
      throws InterruptedException, IOException {
      SnowGlobeData data = source.getData(run, filePath, launcher, taskListener);

    try {
      if (action.equals("create")) {
        System.out.println("Create");
      }
      if (action.equals("apply")) {
        System.out.println("Apply");
      }
      if (action.equals("Destroy")) {
        System.out.println("Destroy");
      }
    }
    finally{
      SnowGlobeAction action = run.getAction(SnowGlobeAction.class);
      if( action == null ) {
        action = new SnowGlobeAction(run);
        run.addAction(action);
      }

      action.save(data);
      run.save();
    }
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  @Extension
  public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "SnowGlobe";
    }

    public static DescriptorExtensionList<GlobeSource,GlobeSourceDescriptor> getOptionList() {
      return GlobeSourceDescriptor.all();
    }
  }
}
