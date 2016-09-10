package com.nirima.snowglobe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ThreadLog extends ThreadLogBase {

  public ByteArrayOutputStream os;

  public boolean enabled ()
  {
    return os != null;
  }

  public void start() {
    this.os = new ByteArrayOutputStream();
  }
  
  public void stop() {
    this.os = null;
  }

  public void write(byte[] bytes) throws IOException {
    if( os == null )
      return;

    //System.out.println("LOG : " + new String(bytes));

    os.write(bytes);
  }

  public String getMessages() {
    if( os == null )
      return "";

    try
    {
      return new String(os.toByteArray(), "UTF-8");
    }
    catch(Exception ex)
    {
      return "Error " + ex;
    }
  }
}
