package com.nirima.snowglobe.web.data;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

@Singleton

public class ProgressManager {
  private static final Logger log = LoggerFactory.getLogger(ProgressManager.class);

  public static ProgressManager _instance;

  /**
   * Map of topic IDs to an entry (I.E a session). Sending to this topic will send to the
   * websocket.
   */
  Map<String, Entry> sessionMap = new HashMap<>();

  public void closeEntry(String topic) {

    Session s = getEntry(topic).session;
    if( s != null)
        s.close();
    getEntry(topic).session = null;
  }

  public static class Entry {
    Session session;

    public void sendString(String s) {
      if( session == null ) {
        log.debug("Not sending to empty session");
        return;
      }
      try {
        session.getRemote().sendString(s);
      } catch (IOException e) {
        // Likely the remote was just closed
      }
    }
  }


  public ProgressManager() {
    _instance = this;
  }

  public void register(Session session) {
    if( session == null )
      throw new IllegalArgumentException("Session must not be null");

    String topic = getTopic(session);
    getEntry(topic).session = session;

  }

  public Entry getEntry(String topic) {
    if( !sessionMap.containsKey(topic) ) {
      sessionMap.put(topic, new Entry());
    }
    return sessionMap.get(topic);
  }

  private static String getTopic(Session session) {

    // Topic is passed as topic? parameter

    WebSocketSession wss = (WebSocketSession) session;

    List<NameValuePair> params = URLEncodedUtils.parse(wss.getRequestURI(), "UTF-8");
    String topic = null;

    for (NameValuePair param : params) {
      if( param.getName().equals("topic"))
        topic = param.getValue();
    }
    return topic;
  }
}
