package com.nirima.jenkins.plugins.snowglobe.workflow;


import com.nirima.jenkins.plugins.snowglobe.calls.SnowGlobeData;
import com.nirima.jenkins.plugins.snowglobe.registry.SnowGlobeRegistry;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;


/**
 * Created by magnayn on 12/09/2016.
 */
public class SnowGlobeWorkflowStep extends AbstractStepImpl implements Serializable {


  @DataBoundConstructor
  public SnowGlobeWorkflowStep() { //String dummy) {

  }


    public static SnowGlobeData getScriptData(FilePath filePath, String path) throws IOException, InterruptedException {
    final FilePath sgFile = new FilePath(filePath, path);

    System.out.println("Get Data for script @ " + sgFile.toString());

    return sgFile.act(new MasterToSlaveFileCallable<SnowGlobeData>() {
      @Override
      public SnowGlobeData invoke(File file, VirtualChannel virtualChannel)
          throws IOException, InterruptedException {
        System.out.println("Get Data for script @ " + file);

        return new SnowGlobeData(new String(FileUtils.readFileToByteArray(file), "UTF-8"), null);
      }
    });
  }

  public static class SourceExection implements Serializable {

    private final Execution execution;

    SourceExection(Execution execution) {
      this.execution = execution;
    }

    @Whitelisted
    BuildExecution fromFile(String file) throws IOException, InterruptedException {
      return new BuildExecution(execution, SnowGlobeWorkflowStep.getScriptData(execution.filePath, file));
    }

    @Whitelisted
    BuildExecution fromString(String string) {
      SnowGlobeData data = new SnowGlobeData(string, null);
      return new BuildExecution(execution,data);
    }
  }

  public static class BuildExecution implements Serializable  {
    SnowGlobeData data;

    private final Execution execution;

    public BuildExecution(Execution run, SnowGlobeData data) {
      this.execution = run;
      this.data = data;
    }

    @Whitelisted
    public void apply() throws IOException {
      data.apply();
    }
    @Whitelisted
    public void destroy() throws IOException {
      data.destroy();
    }

    @Whitelisted
    public SnowGlobeRegister register() {
      return new SnowGlobeRegister(this);
    }

  }

  public static class SnowGlobeRegister implements Serializable  {

    private BuildExecution data;

    private String id;

    private boolean withRun = true;

    @Whitelisted
    public SnowGlobeRegister(BuildExecution snowGlobeData) {
      this.data = snowGlobeData;
      this.id = UUID.randomUUID().toString();
    }

    @Whitelisted
    public SnowGlobeRegister withId(String id) {
      this.id = id;
      return this;
    }
    @Whitelisted
    public SnowGlobeRegister withRun(boolean b) {
      withRun = b;

      return this;
    }
    @Whitelisted
    public BuildExecution exec() throws IOException {
      if( !withRun )
        SnowGlobeRegistry.get().register(id, data.data);
      else
        SnowGlobeRegistry.get().register(id, data.data, data.execution.run);

      return data;
    }

  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl)super.getDescriptor();
  }

  public static class Execution extends AbstractSynchronousStepExecution<SourceExection> {

    static final long serialVersionUID = 1L;


    @StepContextParameter
    private transient TaskListener taskListener;

    @StepContextParameter
    private transient FilePath filePath;

    @StepContextParameter
    private transient Run run;

    @StepContextParameter
    private transient Launcher launcher;

    @Inject
    private transient SnowGlobeWorkflowStep step;


    protected SourceExection run() throws Exception {
      return new SourceExection(this);
    }
  }


  @Extension
  public static class DescriptorImpl extends AbstractStepDescriptorImpl {

    public DescriptorImpl() {
      super(Execution.class);
    }

    public DescriptorImpl(
        Class<? extends StepExecution> executionType) {
      super(executionType);
    }

    @Override
    public String getFunctionName() {
      return "snowglobe";
    }

    @Override
    public String getDisplayName() {
      return "Make SnowGlobes";
    }


  }




}
