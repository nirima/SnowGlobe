package com.nirima.jenkins.plugins.snowglobe.action;

import com.nirima.jenkins.plugins.snowglobe.Consts;
import com.nirima.jenkins.plugins.snowglobe.SnowGlobePluginConfiguration;
import com.nirima.snowglobe.SGExec;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.DependencyGraph;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins;


/**
 * SnowGlobes linked to a job (action badge)
 */
public class SnowGlobeAction implements Action, Serializable,
                                                        Describable<RegisteredScriptAction> {

  private static final Logger LOGGER = Logger.getLogger(SnowGlobeAction.class.getName());

  private Map<String, SnowGlobeData> states = new HashMap<>();
  public final RunLink runLink;

  public SnowGlobeAction(Run<?, ?> run) {
    runLink = new RunLink(run);
  }

  public SnowGlobeData createSnowglobe(RegisteredScript script) {
  //  SnowGlobeData data = new SnowGlobeData(script.scri  return null;pt,null);
  //  states.add( data );
  //  return data;
    return null;
  }

  public SnowGlobeData createSnowglobe(String script) {
      return null;
  }

  public SnowGlobeData getDataById(String id) {
    return states.get(id);
  }

  public Collection<SnowGlobeData> getStates() {
    return states.values();
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
    String path = req.getRestOfPath();

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
    Run<?,?> run = runLink.getRun();
    getDataById( id ).apply();
    run.save();
  }

  private void destroy(String id) throws IOException {
    Run<?,?> run = runLink.getRun();
    getDataById( id ).destroy();
    run.save();
  }

  public void save(SnowGlobeData data) {
      states.put(data.id,data);
  }

  /**
   * Just for assisting form related stuff.
   */
  @Extension
  public static class DescriptorImpl extends Descriptor<RegisteredScriptAction> {
    public String getDisplayName() {
      return "SnowGlobe";
    }
  }
}
