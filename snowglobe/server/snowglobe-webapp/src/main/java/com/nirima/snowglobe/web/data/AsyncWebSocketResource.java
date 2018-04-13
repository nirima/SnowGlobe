package com.nirima.snowglobe.web.data;


import com.google.common.base.Objects;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

/**
 * Service to talk websockets to the client, to stream results from things like 'apply'.
 */
@WebSocket
@ServerEndpoint(value = "/progress")
public class AsyncWebSocketResource {

  private static final Logger log = LoggerFactory.getLogger(AsyncWebSocketResource.class);

  //@Inject ?? Bind through guice? How?
  ProgressManager progressManager;

  public AsyncWebSocketResource() {
    log.info("NEW AsyncWebSocketResource");
    this.progressManager = ProgressManager._instance;
  }


    @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    log.info("Close: statusCode=" + statusCode + ", reason=" + reason);;
  }

  @OnWebSocketError
  public void onError(Throwable t) {
    System.out.println("Error: " + t.getMessage());
  }

  @OnWebSocketConnect
  public void onConnect(Session session) throws IOException {
    // a web socket gets connected
    log.info("OnWebSocketConnect");
    log.info("Connect from {} topic {}.", session.getRemoteAddress().getAddress());

    progressManager.register(session);

    session.getRemote().sendString("Connection established\n");
  }



  @OnWebSocketMessage
  public void onMessage(Session session, String s) {
    log.info("OnWebSocketMEssage " + s);

  }
  public void onMessage(byte[] payload,
                        int offset,
                        int length) {
     log.info("OnWebSocketMEssage");


  }
  

  @OnOpen
  public void onWebSocketConnect(javax.websocket.Session session) {
    log.info("Socket Connected, session: {}", session);

  }

  @OnClose
  public void onWebSocketClose(javax.websocket.Session session, CloseReason reason) {
    log.info("Socket Closed, session: {}, reason: {}", session, reason);

  }

  @OnError
  public void onWebSocketError(javax.websocket.Session session, Throwable throwable) {
    log.error("An error occurred while communicating with the client, session: {}", session,
              throwable);

  }

}
