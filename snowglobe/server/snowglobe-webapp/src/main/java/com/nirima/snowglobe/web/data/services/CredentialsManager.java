package com.nirima.snowglobe.web.data.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nirima.snowglobe.environment.SnowglobeEnvironment;
import com.nirima.snowglobe.repository.Credentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

/**
 * Service for managing credentials
 */
public class CredentialsManager {

  private CredentialStore credentialStore;
  final SnowglobeEnvironment environment;

  public CredentialsManager() throws IOException {
    this.environment = SnowglobeEnvironment.build();
    load();
  }

  public void load() throws IOException {

    File location = getCredentialsFile();
    if( location.exists() ) {
      try(InputStream inputStream = new FileInputStream(location)) {
        this.credentialStore = loadCredentials(inputStream);
      }
    } else {
      credentialStore = new CredentialStore();
    }
  }

  private File getCredentialsFile() {
    File rootDir;

    String env = environment.getFileRepositoryRoot();
    if( env == null ) {
      File r1 = new File(System.getProperty("user.home"));
      rootDir = new File(r1, ".snowglobe");
    } else {
      rootDir = new File(env);
    }

    return new File(rootDir, ".credentialStore");

  }

  public void save() throws IOException {

    try(OutputStream out = new FileOutputStream(getCredentialsFile())) {
      saveCredentials(credentialStore, out);
    }
  }

  protected static CredentialStore loadCredentials(InputStream inputStream) throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(inputStream, CredentialStore.class);
  }

  public static void saveCredentials(CredentialStore store, OutputStream out) throws IOException {
      final ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(out, store);
  }

  public Credentials getCredentialsForLocation(URL url) {
    return credentialStore.getCredentialsByName(url.getHost());
  }

  public void saveCredentialsForLocation(URL url, Credentials credentials) throws IOException {
    credentialStore.saveCredentialsByName(url.getHost(), credentials);
    save();
  }

  public Collection<String> list() {
    return credentialStore.entries.keySet();
  }
}
