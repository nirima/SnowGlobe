package com.nirima.snowglobe.web.data.services;

import com.nirima.snowglobe.repository.Credentials;
import com.nirima.snowglobe.repository.Secret;

public class CredentialEntry {
  public String username;
  public String data;

  public Credentials getCredentials(Secret.SKey key) {
    return new Credentials(this.username, getPassword(key));
   }

  private String getPassword(Secret.SKey key) {

    String passwordCrypt = data;
    String password = new String(Secret.fromCryptedString(passwordCrypt).decrypt(key));

    return password;
  }
}
