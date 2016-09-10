package com.nirima.snowglobe.web.data.services;

import com.nirima.snowglobe.sinatra.model.Customer;
import com.nirima.snowglobe.sinatra.model.Environment;
import com.nirima.snowglobe.sinatra.model.Service;
import com.nirima.snowglobe.sinatra.model.Sinatra;
import com.nirima.snowglobe.sinatra.model.SinatraSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

/**
 * Created by magnayn on 28/04/2017.
 */
public class SinatraManager {
  public Sinatra getSinatra() throws MalformedURLException, FileNotFoundException {

    SinatraSystem ss = new SinatraSystem();
    ss.parseScript(new URL("file://test.dsl"), new FileInputStream(new File("/Users/magnayn/dev/nirima/snowglobe/snowglobe/snowglobe-sinatra/src/test/resources/test.dsl")));
    Sinatra sval = ss.runScript();

    return sval;
  }

  public Service getService(String sID) throws MalformedURLException, FileNotFoundException {
    Sinatra s = getSinatra();

    return (Service)s.getItemByFullId(sID);
  }

  public Optional<Environment> getEnvironment(String sId)
      throws MalformedURLException, FileNotFoundException {

    Sinatra s = getSinatra();

    Environment ss = (Environment)s.getItemByFullId(sId);

    return Optional.ofNullable(ss);
  }
}
