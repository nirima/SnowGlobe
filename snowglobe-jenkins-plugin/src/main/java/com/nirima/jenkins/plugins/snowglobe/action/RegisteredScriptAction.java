package com.nirima.jenkins.plugins.snowglobe.action;

import com.nirima.jenkins.plugins.snowglobe.Consts;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import jenkins.model.Jenkins;

/**
 * Created by magnayn on 09/09/2016.
 */
@ExportedBean
public class RegisteredScriptAction implements Action, Serializable,
                                               Describable<RegisteredScriptAction> {

  public final RunLink runLink;

  public List<RegisteredScript> scripts = new ArrayList<>();

  public RegisteredScriptAction(Run<?, ?> run)
  {
    runLink = new RunLink(run);
  }

  public RegisteredScript addScript(String script, String id) {
    RegisteredScript s = new RegisteredScript(script, id);
    scripts.add( s );
    return s;
  }

  public RegisteredScript getScript(String id) {
    for(RegisteredScript script : scripts) {
      if( script.id.equals(id))
        return script;
    }
    return null;
  }


  @Override
  public String getIconFileName() {
    return  "/plugin/snowglobe-jenkins-plugin/images/32x32/snow-globe.png";
  }

  @Override
  public String getDisplayName() {
    return "Launch Snowglobe";
  }

  @Override
  public String getUrlName() {
    return "snowglobe";
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
  }

  public String getJsUrl(String jsName) {
    return Consts.PLUGIN_JS_URL + jsName;
  }

  public void doControlSubmit(@QueryParameter("launchId") String launchId, StaplerRequest req, StaplerResponse rsp) throws
                                                                                                                    ServletException,
                                                                                                                    IOException
  {
    launchScript(launchId);
    rsp.sendRedirect("snowglobeAction");
  }

  private void launchScript(String launchId) throws IOException {
    Run<?,?> run = runLink.getRun();
    getScript(launchId).build( run ).apply();
    run.save();
  }

  /**
   * Just for assisting form related stuff.
   */
  @Extension
  public static class DescriptorImpl extends Descriptor<RegisteredScriptAction> {
    public String getDisplayName() {
      return "SnowGlobe";
    }

    public void launch(String id) {
      System.out.println("launch");
    }
  }
}
