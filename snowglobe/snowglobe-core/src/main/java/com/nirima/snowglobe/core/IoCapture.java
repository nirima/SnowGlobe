package com.nirima.snowglobe.core;


import org.codehaus.groovy.tools.shell.IO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class IoCapture implements Closeable
{
  public final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
  public final IO io;
  public final PrintStream ps;

  private final InputStream emptyInputStream;

  public IoCapture() {
    emptyInputStream = new ByteArrayInputStream(new byte[0]);

    io = new IO(emptyInputStream, byteArrayOutputStream, byteArrayOutputStream);

    ps = new PrintStream(byteArrayOutputStream);
  }

  public void print(String message) {
    ps.print(message);
  }

  public void println(Object message) {
    ps.println(message);
  }

  public String toString() {
    return new String(byteArrayOutputStream.toByteArray());
  }

  @Override
  public void close() throws IOException {
    emptyInputStream.close();
    io.close();
    ps.close();
  }
}