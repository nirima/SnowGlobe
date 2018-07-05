package com.nirima.snowglobe.web.data;

import com.nirima.snowglobe.utils.ThreadLogBase;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TopicLogger extends ThreadLogBase {

  private final ProgressManager.Entry entry;

  public TopicLogger(ProgressManager.Entry entry) {
    super();
    assert entry != null;
    this.entry = entry;
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean enabled() {
    return true;
  }

  @Override
  public void write(byte[] bytes) throws IOException {
    try {
      if( this.rowId != null )
        entry.sendString("[" + this.rowId.toString() + "]");
      entry.sendString(new String(bytes, "UTF-8"));  
    } catch(Exception ex)
    {
      // No session?
    }
  }
}
