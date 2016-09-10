package com.nirima.snowglobe.server;

import com.google.inject.Injector;

import javax.websocket.server.ServerEndpointConfig;

/**
 * Created by magnayn on 21/06/2017.
 */
public class GuiceConfigurator extends ServerEndpointConfig.Configurator {

  public final Injector injector;

  public GuiceConfigurator(Injector injector) {
    this.injector = injector;
  }

  public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
    return injector.getInstance(endpointClass);
  }


}
