package com.nirima.snowglobe.web.data;


import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.SGParameters;
import com.nirima.snowglobe.core.SnowGlobeSimpleReader;
import com.nirima.snowglobe.core.SnowGlobeSystem;
import com.nirima.snowglobe.repository.IRepositoryModule;
import com.nirima.snowglobe.utils.ThreadLog;
import com.nirima.snowglobe.web.data.services.GlobeManager;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/data")
public class GlobeResource {

  public static class GlobeException extends WebApplicationException {

    public GlobeException(Exception ex) {
      super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(stringOf(ex))
                .type(MediaType.TEXT_PLAIN).build());
    }

    private static final String stringOf(Exception ex) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
      return ex.toString() + "\n" + exceptionAsString;
    }
  }

  @Inject
  ProgressManager progressManager;


  @Inject
  GlobeManager globeManager;

  @Path("/globes")
  @GET
  @Produces("application/json")
  public Collection<Globe> getGlobes() {
    return globeManager.list();
  }

  @Path("/globe/{id}")
  @GET
  @Produces("application/json")
  public Globe globeInfo(@PathParam("id") String id) throws IOException {
    return globeManager.forGlobe(id).details();
  }

  /**
   * Read/Write the config
   **/
  @Path("/globe/{id}/state")
  @GET
  @Produces("text/plain")
  public String getGlobeState(@PathParam("id") String id) throws IOException {
    String state = globeManager.forGlobe(id).getState();
    return state;
  }

  @Path("/globe/{id}/state")
  @Produces("application/json")
  @GET
  public SnowGlobeSimpleReader getGlobeStateJson(@PathParam("id") String id) throws IOException {
    String state = globeManager.forGlobe(id).getState();

    SnowGlobeSystem snowGlobeSystem = new SnowGlobeSystem();
    return snowGlobeSystem.parseStateOnly(new ByteArrayInputStream(state.getBytes()));
  }

  @Path("/globe/{id}/state")
  @PUT
  public void setGlobeState(@PathParam("id") String id, String data) throws IOException {
    globeManager.forGlobe(id).setState(data);
  }

  @Path("/globe/{id}/config/{name}")
  @GET

  public String getGlobeConfig(@PathParam("id") String id, @PathParam("name") String name)
      throws IOException {
    return globeManager.forGlobe(id).getConfig(name);
  }

  @Path("/globe/{id}/config/{name}")
  @PUT
  public void setGlobeConfig(@PathParam("id") String id, @PathParam("name") String name,
                             String data) throws IOException {
    globeManager.forGlobe(id).setConfig(name, data);
  }

  @Path("/globe/{id}/apply")
  @POST
  @Produces("application/json")
  public String apply(@PathParam("id") String id, @QueryParam("async") boolean async, String body)
      throws IOException, GlobeException {

    SGParameters parameters = makeParameters(body);
    if (async) {
      return applyAsync(id, parameters);
    } else {
      return applySync(id, parameters);
    }

  }

  @Path("/globe/{id}/destroy")
  @POST
  @Produces("application/json")
  public String destroy(@PathParam("id") String id) throws IOException, GlobeException {

    SGParameters parameters = new SGParameters();
    SGExec exec = globeManager.forGlobe(id).getSGExec(parameters);

    try {
      String txt = "Result:\n" + exec.destroy() + "\n";

      txt = txt + "Messages:\n" + ((ThreadLog) ThreadLog.get()).getMessages();
      return txt;
    } finally {
      String output = exec.save();
      globeManager.forGlobe(id).setState(output);
    }
  }

  @Path("/globes/clone/{id}")
  @POST
  @Produces("application/json")
  public void clone(@PathParam("id") String id)
      throws IOException, GlobeException, GitAPIException, URISyntaxException {
    globeManager.clone(id);
  }

  private SGParameters makeParameters(String body) throws IOException {
    Properties properties = new Properties();
    properties.load(new ByteArrayInputStream(body.getBytes()));
    SGParameters sgp = new SGParameters(properties);
    return sgp;
  }

  private String applyAsync(String id, SGParameters parameters) {

    // ASYNC
    SGExec exec = globeManager.forGlobe(id).getSGExec(parameters);

    final String topic = UUID.randomUUID().toString();

    Runnable r = new Runnable() {
      public void run() {

        try {
          ProgressManager.Entry entry = progressManager.getEntry(topic);

          try {
            entry.session.getRemote().sendString("--WAKA WAKA--");
          } catch (IOException e) {
            e.printStackTrace();
          }

          ThreadLog.set(new TopicLogger(entry)).start();

          exec.apply();
        } finally {
          String output = exec.save();

          try {
            globeManager.forGlobe(id).setState(output);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    };

    new Thread(r).start();

    return topic;
  }

  private String applySync(String id, SGParameters parameters) {
    ThreadLog.get().start();
    try {

      SGExec exec = globeManager.forGlobe(id).getSGExec(parameters);

      try {
        String txt = "Result:\n" + exec.apply() + "\n";
        txt = txt + "Messages:\n" + ((ThreadLog) ThreadLog.get()).getMessages();
        return txt;
      } finally {
        String output = exec.save();
        globeManager.forGlobe(id).setState(output);
      }
    } catch (Exception ex) {
      throw new GlobeException(ex);
    } finally {
      ThreadLog.get().stop();
    }
  }

  @Path("/globe/{id}/create")
  @PUT
  @Produces("application/json")
  public void create(@PathParam("id") String id) throws IOException {
    globeManager.forGlobe(id).create();
  }

  @Path("/globe/{id}")
  @DELETE
  @Produces("application/json")
  public void delete(@PathParam("id") String id) throws IOException {
    globeManager.forGlobe(id).delete();
  }

  @Path("/globe/{id}/clone/{newId}")
  @POST
  @Produces("application/json")
  public void clone(@PathParam("id") String id, @PathParam("newId") String newId,
                    @QueryParam("includeState") boolean includeState) throws IOException {
    IRepositoryModule newItem = globeManager.forGlobe(newId);
    IRepositoryModule oldItem = globeManager.forGlobe(id);

    newItem.create();
    newItem.setConfig(null, oldItem.getConfig(null));

    if (includeState) {
      newItem.setState(oldItem.getState());
    }
  }

  @Path("/globe/{id}/graph")
  @GET
  @Produces("image/png")
  public byte[] getGraph(@PathParam("id") String id) throws Exception {

    SGExec exec = globeManager.forGlobe(id).getSGExec(new SGParameters());

    ByteArrayOutputStream graphString = new ByteArrayOutputStream();
    exec.graph(graphString);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    runDot(baos, new ByteArrayInputStream(graphString.toByteArray()), "png");

    return baos.toByteArray();
  }

  @Path("/globe/{id}/validate")
  @GET
  public void validate(@PathParam("id") String id) throws Exception {

    ThreadLog.get().start();
    try {
      SGExec exec = globeManager.forGlobe(id).getSGExec(new SGParameters());

    } catch (Exception e) {
      throw new GlobeException(e);
    } finally {
      ThreadLog.get().stop();
    }
  }


  @Path("/hello/{id}")
  @GET
  @Produces("application/json")
  public String getHello(@PathParam("id") String id) throws Exception {

    return "Hello " + id;
  }


  /**
   * Execute the dot command with given input and output stream
   *
   * @param type the parameter for the -T option of the graphviz tools
   */
  protected void runDot(OutputStream op, InputStream input, String type)
      throws IOException {

    String dotPath;

    if (new File("/usr/bin/dot").exists()) {
      dotPath = "/usr/bin/dot";
    } else {
      dotPath = "/usr/local/bin/dot";
    }

    byte[] output = null;
    try {
      output = new ProcessExecutor().command(dotPath, "-T" + type, "-Gcharset=UTF-8", "-q1")
          .redirectInput(input)
          .readOutput(true).execute().output();

      op.write(output);

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }


  }


}
