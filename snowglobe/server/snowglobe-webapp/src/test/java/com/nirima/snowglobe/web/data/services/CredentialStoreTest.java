package com.nirima.snowglobe.web.data.services;

import com.nirima.snowglobe.repository.Credentials;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.testng.Assert.*;

public class CredentialStoreTest {

  @Test
  public void testSaveCredentialsByName()  {
    try {
      CredentialStore credentialStore = new CredentialStore();
      credentialStore.saveCredentialsByName("github.com", new Credentials("woo", "yay"));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      CredentialsManager.saveCredentials(credentialStore, baos);

      baos.close();
      String data = baos.toString();

      System.out.println("DATA: " + data);

      credentialStore = CredentialsManager.loadCredentials( new ByteArrayInputStream(data.getBytes()));
      Credentials c = credentialStore.getCredentialsByName("github.com");
      System.out.println(c.getUsername());
      System.out.println(c.getPassword());

    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}