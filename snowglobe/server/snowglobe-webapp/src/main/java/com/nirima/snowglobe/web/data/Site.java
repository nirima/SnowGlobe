package com.nirima.snowglobe.web.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by magnayn on 27/04/2017.
 */
public class Site implements Serializable {

  public String id;
  public String name;
  public List<Environment> environments = new ArrayList<>();

  public Site(String id, String s) {
    this.id = id;
    this.name = s==null?id:s;
  }

  public static class Environment {

    public String id;
    public String name;
    public List<EnvData> hosts = new ArrayList<>();

    public Environment(String id, String s) {
      this.id = id;
      this.name = s==null?id:s;
    }
  }

}
