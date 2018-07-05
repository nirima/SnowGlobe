package com.nirima.snowglobe.web.data;

import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.core.SnowGlobeSimpleReader;
import com.nirima.snowglobe.core.SnowGlobeSystem;
import com.nirima.snowglobe.repository.Credentials;
import com.nirima.snowglobe.repository.IRepositoryItem;
import com.nirima.snowglobe.repository.ITransactionalRepositoryItem;
import com.nirima.snowglobe.utils.ThreadLog;
import com.nirima.snowglobe.web.data.services.CredentialsManager;
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
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

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


@Path("/credentials")
public class CredentialsResource {
  @Inject
  CredentialsManager credentialsManager;

  @Path("/list")
  @GET
  public Collection<String> list() {
    return credentialsManager.list();
  }


}
