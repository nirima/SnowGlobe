package com.nirima.snowglobe.repository;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class Credentials {

  private String password;
  private String username;

  public Credentials(String username, String password ) {
    this.password = password;
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public CredentialsProvider getCredentialsProvider() {

    return new UsernamePasswordCredentialsProvider(username, password);
  }
}
