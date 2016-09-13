package com.nirima.jenkins.plugins.snowglobe.calls;

import com.nirima.jenkins.plugins.snowglobe.SnowGlobePluginConfiguration;
import com.nirima.jenkins.plugins.snowglobe.registry.SnowGlobeRegistry;
import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.core.Core;
import com.nirima.snowglobe.jenkins.JenkinsDockerProvider;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by magnayn on 09/09/2016.
 */
public class SnowGlobeData implements Serializable {

  public String script;
  public String state;

  public String lastResult;

  public SnowGlobeData(String script, String state) {

    this.script = script;
    this.state = state;
  }

  @Whitelisted
  public SnowGlobeData apply()
      throws IOException {
    InputStream a = new ByteArrayInputStream(script.getBytes("UTF-8"));
    InputStream s = null;
    if( state != null )
      s = new ByteArrayInputStream(state.getBytes("UTF-8"));

    SGExec exec = new SGExec(a, s );

    try {

      exec.apply();
      lastResult = "[OK]";
    } catch(Exception ex) {
      lastResult = ex.toString();
    }
    finally {
      state = exec.save();

    }
    return this;
  }

  @Whitelisted
  public SnowGlobeData destroy()
      throws IOException {
    InputStream a = new ByteArrayInputStream(script.getBytes("UTF-8"));
    InputStream s = null;
    if( state != null )
      s = new ByteArrayInputStream(state.getBytes("UTF-8"));

    SGExec exec = new SGExec( a, s );

    try {
      exec.destroy();
      lastResult = "[OK]";
    } catch(Exception ex) {
      lastResult = ex.toString();
    }
    finally {
      state = exec.save();

    }
    return this;
  }

  @Whitelisted
  public String graph() throws IOException {
    InputStream a = new ByteArrayInputStream(script.getBytes("UTF-8"));
    InputStream s = null;
    if( state != null )
      s = new ByteArrayInputStream(state.getBytes("UTF-8"));

    SGExec exec = new SGExec( a, s );


    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    exec.graph(baos);
    baos.close();
    return new String(baos.toByteArray(),"UTF-8");

  }

}
