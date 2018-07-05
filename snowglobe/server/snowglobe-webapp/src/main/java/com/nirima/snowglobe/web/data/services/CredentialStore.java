package com.nirima.snowglobe.web.data.services;

import com.nirima.snowglobe.repository.Credentials;
import com.nirima.snowglobe.repository.Secret;

import java.util.HashMap;
import java.util.Map;

public class CredentialStore {
  public Secret.SKey key;
  public Map<String, CredentialEntry> entries = new HashMap<>();

  public CredentialStore() {
    this.key = Secret.SKey.random();
  }

  public CredentialEntry getEntryByName(String name) {
    return entries.get(name);
  }

  public Credentials getCredentialsByName(String host) {
    CredentialEntry entry =   getEntryByName(host);
    if( entry == null ) return null;
    return entry.getCredentials(key);
  }

  public void saveCredentialsByName(String host, Credentials credentials) {
    CredentialEntry entry = new CredentialEntry();

    Secret secret = Secret.encrypt(credentials.getPassword(), key);

    entry.username = credentials.getUsername();
    entry.data = secret.toString();

    entries.put(host, entry);

  }
}
