package com.nirima.snowglobe.repository;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.security.SecureRandom;


public class SecretTest {


  @Test
  public void testSF() throws Exception {

    Secret.SKey bb = Secret.SKey.random();
    System.out.println(bb.toString());

    bb = new Secret.SKey("N9WuZpF76AEUzTmwiJO98A==");

   Secret xxx = Secret.encrypt("Hello", bb);

   System.out.println(xxx);
   System.out.println(Secret.fromCryptedString(xxx.toString()));



    String yyy = new String(xxx.decrypt(bb));


    Assert.assertEquals("Hello", yyy);
  }
}