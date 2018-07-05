package com.nirima.snowglobe.docker;

import java.util.List;

public class DockerManifestSummary {
  public int schemaVersion;
  public String mediaType;
  public Config config;
  public List<Layer> layers;

  public static class Config {
    public String mediaType;
    public long size;
    public String digest;
  }

  public static class Layer {
    public String mediaType;
    public long size;
    public String digest;
    
  }


}
