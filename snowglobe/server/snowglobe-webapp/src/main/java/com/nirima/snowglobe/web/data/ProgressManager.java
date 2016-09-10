package com.nirima.snowglobe.web.data;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class ProgressManager {

  public static ProgressManager _instance;

  public static class Entry {
    Session session;
  }
  Map<String, Entry> sessionMap = new HashMap<>();


  public ProgressManager() {
    _instance = this;
  }

  public void register(Session session) {
    String topic = getTopic(session);
    getEntry(topic).session = session;


  }

  public Entry getEntry(String topic) {
    if( !sessionMap.containsKey(topic) ) {
      sessionMap.put(topic, new Entry());
    }
    return sessionMap.get(topic);
  }

  public static String getTopic(Session session) {
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
