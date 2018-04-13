package com.nirima.snowglobe.web.docker;

import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.SGParameters;
import com.nirima.snowglobe.core.Provider;
import com.nirima.snowglobe.core.ProviderContext;
import com.nirima.snowglobe.docker.DockerProvider;
import com.nirima.snowglobe.web.data.services.GlobeManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/docker")
public class DockerResource {

  @Inject
  GlobeManager globeManager;

  @Path("/endpoints")
  @GET
  @Produces("application/json")
  public Collection<DockerServer> getDockerServers() {
    List<DockerServer> items = new ArrayList<>();

    // Find all the docker endpoints that we know about
    globeManager.list().forEach(globe -> {

                                  try {
                                    SGExec sge = globeManager.forGlobe(globe.id).getSGExec();

                                    sge.sgContext.getModules().forEach(
                                        module -> {
                                          module.getProviders().forEach(
                                              providerContext -> {
                                                Provider p = providerContext.getProxy();
                                                if (p instanceof DockerProvider) {

                                                  String id = p.getId();
                                                  System.out.println("Docker provider " + id);
                                                  System.out.println("Module ID " + module.getProxy().id);
                                                  System.out.println("GlobeID " + globe.id);

                                                  String uri = globe.id + ":" + module.getProxy().id + ":";
                                                  if( id != null )
                                                    uri += id;

                                                  DockerServer ds = new DockerServer();
                                                  ds.id = uri;
                                                  ds.host = ((DockerProvider) p).host;

                                                  items.add(ds);

                                                }
                                              }
                                          );
                                        }
                                    );

                                  } catch (Exception ex) {
                                    ex.printStackTrace();
                                  }
                                }

    );

    return items;
  }
}
