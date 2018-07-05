
/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * Copyright (c) 2016, CloudBees Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.nirima.snowglobe.repository;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Bytes;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Secret implements Serializable {

  public static class SKey implements Serializable {
    public byte[] key;

    public SKey(){}

    public SKey(byte[] bb) {
      this.key = bb;
    }

    public SKey(String key) {
      this.key = Base64.getDecoder().decode(key.getBytes());
    }

    public static SKey random() {
      SecureRandom sr = new SecureRandom();
      byte[] bb = sr.generateSeed(16);
      return new SKey(bb);
    }

    @Override
    public String toString() {
      return new String(Base64.getEncoder().encode(key));
    }

    SecretKeySpec makeKey() {
      try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        //byte[] key = md.digest(encryptionKey.getBytes("UTF-8"));
        return new SecretKeySpec(key, "AES");
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }

      return null;
    }
  }

  public static final int IV_SIZE=16;

  private byte[] iv;
  private byte[] cipherBytes;

  public Secret(byte[] iv, byte[] cipherBytes) {
    this.iv = iv;
    this.cipherBytes = cipherBytes;
  }

  public static Secret fromCryptedString(String string) {
    byte[] data = Base64.getDecoder().decode(string);


    byte[] iv = new byte[IV_SIZE];
    byte[] idata = new byte[data.length - IV_SIZE];

    System.arraycopy(data,0,iv,0,IV_SIZE);
    System.arraycopy(data,IV_SIZE,idata,0,idata.length);

    return new Secret(iv,idata);

  }

  public String toString() {
    Base64.Encoder encoder = Base64.getEncoder();


    byte[] ivB = encoder.encode(Bytes.concat(iv,cipherBytes));


    return new String(ivB);

  }

  public byte[] decrypt(SKey key) {
    return decrypt(cipherBytes, key, iv);
  }

  public static Secret encrypt(String data, SKey key) {
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      byte[] iv = generateRandomIv();
      cipher.init(Cipher.ENCRYPT_MODE, key.makeKey(),new IvParameterSpec( iv ));
      byte[] cipherBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

      return new Secret(iv, cipherBytes);


    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] decrypt(byte[] cipherBytes, SKey key, byte[] iv )  {

    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

      cipher.init(Cipher.DECRYPT_MODE,key.makeKey(),new IvParameterSpec( iv ));
    //  byte[] cipherBytes = Base64.getDecoder().decode(removeIvFromString(data));
      return cipher.doFinal(cipherBytes);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private static AlgorithmParameterSpec makeIv(String iv) {
    try {
      return new IvParameterSpec(iv.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static byte[] generateRandomIv() {
    return new SecureRandom().generateSeed(16);
  }

  private String getIv(String data) {
    return data.substring(0, data.indexOf(System.getProperty("line.separator")));
  }
  private String removeIvFromString(String data) {
    return data.substring(data.indexOf(System.getProperty("line.separator")) + 1, data.length());
  }


}