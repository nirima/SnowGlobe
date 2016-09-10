package com.nirima.snowglobe.utils;

import java.io.IOException;

public abstract class ThreadLogBase {
  public static ThreadLocal<ThreadLogBase> tls = new ThreadLocal<>();

  public static ThreadLogBase get() {
    if( tls.get() == null )
      tls.set(new ThreadLog());

    return tls.get();
  }

  public static ThreadLogBase set(ThreadLogBase item) {


    if( tls.get() != null )
      tls.get().stop();

    tls.set(item);

    return tls.get();
  }



  public static ThreadLogBase get(Class klass)
      throws IllegalAccessException, InstantiationException {
    if( tls.get() == null )
      tls.set((ThreadLogBase) klass.newInstance());

    return tls.get();
  }

  public abstract void start();
  public abstract void stop();
  public abstract boolean enabled ();
  public abstract void write(byte[] bytes) throws IOException;

  public void write(String data) throws IOException {
    this.write(data.getBytes());
    this.write("\n".getBytes());
  }

}
