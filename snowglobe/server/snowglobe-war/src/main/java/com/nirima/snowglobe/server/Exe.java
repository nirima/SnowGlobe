package com.nirima.snowglobe.server;

import com.google.inject.Injector;

import com.nirima.snowglobe.web.GuiceServletConfig;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

public class Exe {

  private static Logger LOGGER = LoggerFactory.getLogger(Exe.class);

  public static void main(String[] args) throws Exception {
    Integer port = getPort();

    LOGGER.info("Starting jetty on port: {}", port);
    System.out.println("JETTY " + port );
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

    JettyServer server = new JettyServer(port, createInjector(context.getServletContext()), context);
    Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    server.start();
  }

  private static Integer getPort() {
    String port = System.getenv("PORT");
    if (port == null || port.isEmpty()) {
      port = "8808";
    }

    return Integer.valueOf(port);
  }

  private static Injector createInjector(ServletContext servletContext) {
    return GuiceServletConfig.createInjectorInstance(servletContext);
  }
}
