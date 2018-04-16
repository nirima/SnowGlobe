package com.nirima.snowglobe.web.data;


import com.nirima.snowglobe.SGExec;
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
import java.util.List;
import java.util.Set;
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

  @Path("/version")
  @GET
  public String getVersion() {
    return "0.1";
  }

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

  @Path("/globe/{id}/tags")
  @GET
  @Produces("application/json")
  public Set<String> getGlobeTags(@PathParam("id") String id) throws IOException {
    return globeManager.forGlobe(id).getTags();
  }

  @Path("/globe/{id}/tags")
  @PUT
  @Produces("application/json")
  public void setGlobeTags(@PathParam("id") String id, Set<String> body) throws IOException {
    globeManager.forGlobe(id).setTags(body);
  }

  @Path("/globe/{id}/tag/{tag}")
  @PUT
  @Produces("application/json")
  public void addTag(@PathParam("id") String id, @PathParam("tag")String tag) throws IOException {
    IRepositoryModule rm = globeManager.forGlobe(id);
    Set<String> tags = rm.getTags();
    tags.add(tag);
    rm.setTags(tags);
  }

  @Path("/globe/{id}/tag/{tag}")
  @DELETE
  @Produces("application/json")
  public void removeTag(@PathParam("id") String id, @PathParam("tag")String tag) throws IOException {
    IRepositoryModule rm = globeManager.forGlobe(id);
    Set<String> tags = rm.getTags();
    tags.remove(tag);
    rm.setTags(tags);
  }


  @Path("/globe/{id}/vars")
  @GET
  @Produces("text/plain")
  public String getGlobeVars(@PathParam("id") String id) throws IOException {
    return globeManager.forGlobe(id).getVariables();
  }

  @Path("/globe/{id}/vars")
  @PUT
  @Produces("text/plain")
  public void setGlobeVars(@PathParam("id") String id, String body) throws IOException {
    globeManager.forGlobe(id).setVariables(body);
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
  @PUT
  @Produces("application/json")
  public String apply(@PathParam("id") String id, @QueryParam("async") String async)
      throws IOException, GlobeException {

    try {
      if (async != null) {
        return applyAsync(id, async);
      } else {
        return applySync(id);
      }
    }
    catch (Exception ex) {
      throw new GlobeException(ex);
    }
  }

  @Path("/globe/{id}/destroy")
  @POST
  @Produces("application/json")
  public String destroy(@PathParam("id") String id) throws IOException, GlobeException {

    SGExec exec = globeManager.forGlobe(id).getSGExec();

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


  private String applyAsync(String id, String topic) {

    // ASYNC
    SGExec exec = globeManager.forGlobe(id).getSGExec();

    Runnable r = () -> {
      ProgressManager.Entry entry = progressManager.getEntry(topic);
      try {


        entry.sendString("[[Starting]]\n");


        ThreadLog.set(new TopicLogger(entry)).start();

        exec.apply();
        entry.sendString("[[Completed OK]]\n");
      }
      catch(Exception ex) {
          entry.sendString("[[Completed FAIL]]\n");
          entry.sendString(ex.getMessage());
      }
      finally {
        // Always Save the state.
        String output = exec.save();

        try {
          globeManager.forGlobe(id).setState(output);

        } catch (IOException e) {
            e.printStackTrace();


            entry.sendString("[[Completed /Exception/]]\n");
            entry.sendString(e.getMessage());

        }

        progressManager.closeEntry(topic);
        ThreadLog.get().stop();
      }
    };

    new Thread(r).start();

    return topic;
  }



  private String applySync(String id) throws IOException {
    ThreadLog.get().start();
    try {

      SGExec exec = globeManager.forGlobe(id).getSGExec();

      try {
        String txt = "Result:\n" + exec.apply() + "\n";
        txt = txt + "Messages:\n" + ((ThreadLog) ThreadLog.get()).getMessages();
        return txt;
      } finally {
        String output = exec.save();
        globeManager.forGlobe(id).setState(output);
      }
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

    SGExec exec = globeManager.forGlobe(id).getSGExec();

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
      SGExec exec = globeManager.forGlobe(id).getSGExec();

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
