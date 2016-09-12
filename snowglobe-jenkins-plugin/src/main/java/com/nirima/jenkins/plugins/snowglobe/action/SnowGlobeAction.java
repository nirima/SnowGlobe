package com.nirima.jenkins.plugins.snowglobe.action;

import com.nirima.jenkins.plugins.snowglobe.Consts;
import com.nirima.jenkins.plugins.snowglobe.SnowGlobePluginConfiguration;
import com.nirima.jenkins.plugins.snowglobe.calls.SnowGlobeData;
import com.nirima.jenkins.plugins.snowglobe.registry.SnowGlobeRegistry;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins;


/**
 * SnowGlobes linked to a job (action badge)
 */
public class SnowGlobeAction implements Action, Serializable,
                                                        Describable<SnowGlobeAction> {

  private static final Logger LOGGER = Logger.getLogger(SnowGlobeAction.class.getName());

  private String id;

  public SnowGlobeAction(String runId) {
    this.id = runId;
  }

  public String getId() {
    return id;
  }

  public static class ActionData {
    public String id;
    public SnowGlobeData data;

    public ActionData(String id, SnowGlobeData data) {
      this.id = id;
      this.data = data;
    }

    public String getId() {
      return id;
    }

    public SnowGlobeData getData() {
      return data;
    }
  }


  public SnowGlobeData createSnowglobe(String script) {
      return null;
  }

  public SnowGlobeData getDataById(String id) {
    return SnowGlobeRegistry.get().getById(id);
  }

  public Collection<ActionData> getStates() {
    Set<String> ids =  SnowGlobeRegistry.get().getGlobesForRunId(this.id);

    Set<ActionData> data = new HashSet<>();

    if( ids != null )
      ids.forEach( it -> data.add( new ActionData(it, SnowGlobeRegistry.get().getById(it) )) );

    return data;

  }

  @Override
  public String getIconFileName() {
    return  "/plugin/snowglobe-jenkins-plugin/images/32x32/snow-globe.png";
  }

  @Override
  public String getDisplayName() {
    return "Snowglobe";
  }

  @Override
  public String getUrlName() {
    return "snowglobeAction";
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
  }


  public String getJsUrl(String jsName) {
    return Consts.PLUGIN_JS_URL + jsName;
  }

  public void doControlSubmit(@QueryParameter("action") String action,
                              @QueryParameter("launchId") String id, StaplerRequest req, StaplerResponse rsp) throws
                                                                                                                ServletException,
                                                                                                                IOException,
                                                                                                                InterruptedException {

    if( action.equals("apply") ) {
      apply(id);
    } else if( action.equals("destroy") ) {
      destroy(id);
    }

    rsp.sendRedirect(".");
  }

  public void doDynamic(StaplerRequest req, StaplerResponse rsp)  throws IOException, ServletException, InterruptedException {
    //String path = req.getRestOfPath();

    String id = req.getParameter("id");

    String graphString = getDataById( id ).graph();

    rsp.setContentType("image/png");

      runDot(rsp.getOutputStream(), new ByteArrayInputStream(graphString.getBytes(
          Charset.forName("UTF-8"))), "png");
  }

  /**
   * Execute the dot command with given input and output stream
   * @param type the parameter for the -T option of the graphviz tools
   */
  protected void runDot(OutputStream output, InputStream input, String type)
      throws IOException {

    String dotPath = SnowGlobePluginConfiguration.get().getDotExeOrDefault();
    Launcher launcher = Jenkins.getInstance().createLauncher(new LogTaskListener(LOGGER, Level.CONFIG));
    try {
      launcher.launch()
          .cmds(dotPath,"-T" + type, "-Gcharset=UTF-8", "-q1")
          .stdin(input)
          .stdout(output)
          .start().join();
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Interrupted while waiting for dot-file to be created", e);
    }
    finally {
      if (output != null) {
        output.close();
      }
    }
  }

  private void apply(String id) throws IOException {
    getDataById( id ).apply();
    SnowGlobeRegistry.get().save();
  }

  private void destroy(String id) throws IOException {
    getDataById( id ).destroy();
    SnowGlobeRegistry.get().save();
  }


  /**
   * Just for assisting form related stuff.
   */
  @Extension
  public static class DescriptorImpl extends Descriptor<SnowGlobeAction> {
    public String getDisplayName() {
      return "SnowGlobe";
    }
  }
}
