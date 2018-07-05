package com.nirima.snowglobe.docker;

import java.io.Serializable;

public class DockerAuth implements Serializable {
  public String token;

  public String access_token;

  public int expires_in;

  public String issued_at;

}
