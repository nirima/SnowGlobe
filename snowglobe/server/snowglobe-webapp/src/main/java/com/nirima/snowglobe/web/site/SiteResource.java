package com.nirima.snowglobe.web.site;

import com.nirima.snowglobe.sinatra.model.Customer;
import com.nirima.snowglobe.sinatra.model.Environment;
import com.nirima.snowglobe.sinatra.model.Sinatra;
import com.nirima.snowglobe.web.data.EnvData;
import com.nirima.snowglobe.web.data.Site;
import com.nirima.snowglobe.web.data.services.SinatraManager;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * API for managing sites, resources, customers, etc.
 */
@Path("/sites")
public class SiteResource {

  @Inject
  SinatraManager sinatraManager;

  @Path("/customers")
  @GET
  @Produces("application/json")
  public List<Site> getCustomers() throws MalformedURLException, FileNotFoundException {
    Sinatra sinatra = sinatraManager.getSinatra();

    return sinatra.findAll(Customer.class)
        .map( si -> (Customer) si)
        .map(si -> {

          final Site s = new Site(si.getFullId(), si.name);

          si.getEnvironments().forEach(
              env -> {
                Site.Environment siv = new Site.Environment(env.getFullId(), env.name);
                s.environments.add(siv);
              }
          );


          return s;
        })
        .collect(Collectors.toList());

  }

  @Path("/site/{sid}")
  @GET
  @Produces("application/json")
  public EnvData getSiteDetail(@PathParam("sid") String sId) throws MalformedURLException, FileNotFoundException {
    Sinatra sinatra = sinatraManager.getSinatra();

    Optional<Environment> envOptional = sinatraManager.getEnvironment(sId);

    if( !envOptional.isPresent())
      throw new NotFoundException();

    Environment e = envOptional.get();

    EnvData ed = EnvData.buildFromEnvironment(e);



    return ed;
  }



}
