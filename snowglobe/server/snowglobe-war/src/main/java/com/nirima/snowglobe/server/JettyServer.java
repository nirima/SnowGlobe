package com.nirima.snowglobe.server;

import com.google.inject.Injector;

import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Injector;


import com.nirima.snowglobe.environment.SnowglobeEnvironment;
import com.nirima.snowglobe.web.data.AsyncWebSocketResource;

import org.apache.jasper.servlet.JspServlet;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.websocket.api.InvalidWebSocketException;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
/**
 * Created by magnayn on 21/06/2017.
 */
public class JettyServer {
  private GuiceConfigurator configurator;
  private Server server;
  private int port;

  private ServletContextHandler context;

  public JettyServer(int port, Injector injector, ServletContextHandler sch) {
    this.port = port;
    this.configurator = new GuiceConfigurator(injector);
    this.context = sch;
  }

  public boolean shutdown() {
    try {
      server.stop();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

//  public void start2() throws Exception {
//
//
//
//    Server server = new Server();
//    ServerConnector connector = new ServerConnector(server);
//    connector.setPort(port);
//    server.addConnector(connector);
//
//    URL resources = JettyServer.class.getResource("/webapp");
//    ResourceHandler resource_handler = new ResourceHandler();
//    resource_handler.setDirectoriesListed(true);
//    resource_handler.setWelcomeFiles(new String[]{ "index.html" });
//
//    resource_handler.setBaseResource(Resource.newResource(resources));
//
//    HandlerList handlers = new HandlerList();
//    handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
//    server.setHandler(handlers);
//
//    server.start();
//    server.join();
//  }

  public void start() throws Exception {
    try {
      Server server = new Server(port);

      System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");

      // Add annotation scanning (for WebAppContexts)
      Configuration.ClassList classlist = Configuration.ClassList
          .setServerDefault( server );
      classlist.addBefore(
          "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
          "org.eclipse.jetty.annotations.AnnotationConfiguration" );




      // Find the full path to the webroot.
      // Use the real path, with real file system case for all parts of the path
      // Otherwise we fall afoul of alias checking.
      // (esp on OSX and Windows. Unix and Linux do not have this issue)

      /*Path webrootPath = new File("/Users/magnayn/dev/nirima/audaera/audaera-war/src/main/resources/webapp").toPath().toRealPath();
    URI webrootUri = webrootPath.toUri();

    System.err.println("webroot uri: " + webrootUri);
        */

      Resource webroot = Resource.newResource(getClass().getResource("/webapp"));
      if (!webroot.exists())
      {
        System.err.println("Resource does not exist: " + webroot);
        System.exit(-1);
      }

      if (!webroot.isDirectory())
      {
        System.err.println("Resource is not a directory: " + webroot);
        System.exit(-1);
      }

      // Establish ServletContext for all servlets
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);


/*      context.setClassLoader(
          Thread.currentThread().getContextClassLoader()
      );
  */





      context.setContextPath("/");
      context.setBaseResource(webroot);

      enableEmbeddedJspSupport(context);

      // What file(s) should be used when client requests a directory
      context.setWelcomeFiles(new String[] { "index.html" });
      server.setHandler(context);



      context.setAttribute(Injector.class.getName(),configurator.injector);

      context.addFilter(com.google.inject.servlet.GuiceFilter.class,"/*",null);


      /** Jersey  ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
       jerseyServlet.setInitOrder(0);
       // jerseyServlet.setInitParameter(ServerProperties.APPLICATION_NAME, "org.example.pss.resources");
       jerseyServlet.setInitParameter("javax.ws.rs.Application", "com.nirima.audaera.web.jersey.JerseyApplication");
       **/

      FilterHolder jerseyFilter = context.addFilter(org.glassfish.jersey.servlet.ServletContainer.class, "/*",null);
      jerseyFilter.setInitParameter("javax.ws.rs.Application", "com.nirima.snowglobe.web.jersey.JerseyApplication");


     /*
      ServletHolder holderJsp = new ServletHolder("jsp",JspServlet.class);
      holderJsp.setInitOrder(0);
      context.addServlet(holderJsp, "*.jsp");
       */

      // Add a servlet (technique #1)
      //ServletHolder holderHello = context.addServlet(HelloServlet.class,"/hello");
      // holderHello.setInitOrder(0);


      // *** DEBUG ***/
      SnowglobeEnvironment environment = SnowglobeEnvironment.build();
      if( environment.isDebug() ) {
        ServletHolder secure = new ServletHolder("secure", DefaultServlet.class);
        secure.setInitParameter("dirAllowed", "true");
        secure.setInitParameter("resourceBase", new File(
            "/Users/magnayn/dev/nirima/snowglobe/snowglobe/server/snowglobe-server-ui/target/dist").toURI()
            .toASCIIString());
        secure.setInitParameter("pathInfoOnly", "true");
        context.addServlet(secure, "/secure/*");
      }


      // Add default servlet last (always last) (technique #2)
      // Must be named "default", must be on path mapping "/"
      ServletHolder holderDef = new ServletHolder("default",DefaultServlet.class);
      holderDef.setInitParameter("dirAllowed","true");
      context.addServlet(holderDef,"/");


      ////////////////////////////
      // Add WebSocket Support  //
      ////////////////////////////
      WebSocketHandler wsHandler = new WebSocketHandler() {
        @Override
        public void configure(WebSocketServletFactory factory) {
          factory.register(AsyncWebSocketResource.class);
        }
      };

      ContextHandler contextHandler = new ContextHandler();
      contextHandler.setContextPath("/progress");
      contextHandler.setHandler(wsHandler);

      // Handlers to include WS
      HandlerCollection handlerCollection = new HandlerCollection();

      handlerCollection.setHandlers(new Handler[] { wsHandler, context});

      server.setHandler(handlerCollection);


      // Start server
      server.start();
    }
    catch (MalformedURLException e)
    {
      System.err.println("Unable to establish webroot");
      e.printStackTrace(System.err);
    }
    catch (Throwable t)
    {
      t.printStackTrace(System.err);
    }
  }

  private void enableEmbeddedJspSupport(ServletContextHandler servletContextHandler) throws IOException
  {
    // Establish Scratch directory for the servlet context (used by JSP compilation)
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

    if (!scratchDir.exists())
    {
      if (!scratchDir.mkdirs())
      {
        throw new IOException("Unable to create scratch directory: " + scratchDir);
      }
    }
    servletContextHandler.setAttribute("javax.servlet.context.tempdir", scratchDir);

    // Set Classloader of Context to be sane (needed for JSTL)
    // JSP requires a non-System classloader, this simply wraps the
    // embedded System classloader in a way that makes it suitable
    // for JSP to use
    ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
    servletContextHandler.setClassLoader(jspClassLoader);

    // Manually call JettyJasperInitializer on context startup
    servletContextHandler.addBean(new JspStarter(servletContextHandler));

    // Create / Register JSP Servlet (must be named "jsp" per spec)
    ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
    holderJsp.setInitOrder(0);
    holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
    holderJsp.setInitParameter("fork", "false");
    holderJsp.setInitParameter("xpoweredBy", "false");
    holderJsp.setInitParameter("compilerTargetVM", "1.8");
    holderJsp.setInitParameter("compilerSourceVM", "1.8");
    holderJsp.setInitParameter("keepgenerated", "true");
    servletContextHandler.addServlet(holderJsp, "*.jsp");
  }

//  public void start3() throws Exception {
//    URL resources = JettyServer.class.getResource("/webapp");
//    server = new Server();
//    ServerConnector connector = new ServerConnector(server);
//    connector.setPort(port);
//    server.addConnector(connector);
//
//    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//   // ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
//   // context.addFilter(AuthenticationFilter.class, "/*", null);
//
//    context.setContextPath("/");
//
//
//
//    // Add static files handler
//    context.setBaseResource(Resource.newResource(resources));
//    //context.setResourceBase(resources.toExternalForm());
//
//
//    // Since this is a ServletContextHandler we must manually configure JSP support.
//    enableEmbeddedJspSupport(context);
//
//  //  context.addServlet(DefaultServlet.class, "/");
//  //  context.setWelcomeFiles(new String[]{"index.html"});
//
//    context.setAttribute(Injector.class.getName(),configurator.injector);
//
//    ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
//    jerseyServlet.setInitOrder(0);
//   // jerseyServlet.setInitParameter(ServerProperties.APPLICATION_NAME, "org.example.pss.resources");
//    jerseyServlet.setInitParameter("javax.ws.rs.Application", "com.nirima.audaera.web.jersey.JerseyApplication");
//
//
//    context.addFilter(com.google.inject.servlet.GuiceFilter.class,"/*",null);
//
//
//   // ServletHolder contentServlet = context.addServlet(DefaultServlet.class, "/*");
//   // context.addServlet(contentServlet, "/*");
//
//
//  //  ServerContainer wsContainer = WebSocketServerContainerInitializer.configureContext(context);
//    //wsContainer.addEndpoint(createEndpointConfig(AmpliboxWebSocketHandler.class));
//  //    wsContainer.addEndpoint(EventSocket.class);
//
//
//    WebSocketHandler wsHandler = new WebSocketHandler() {
//      @Override
//      public void configure(WebSocketServletFactory factory) {
//        factory.register(AmpliboxWebSocketHandler.class);
//      }
//    };
//
//        ContextHandler contextHandler = new ContextHandler();
//        contextHandler.setContextPath("/websocket");
//      contextHandler.setHandler(wsHandler);
////
//
//    // Add default servlet last (always last) (technique #2)
//    // Must be named "default", must be on path mapping "/"
//    ServletHolder holderDef = new ServletHolder("default",DefaultServlet.class);
//    holderDef.setInitParameter("dirAllowed","true");
//    context.addServlet(holderDef,"/");
//
//
//    ResourceHandler resource_handler = new ResourceHandler();
//    resource_handler.setDirectoriesListed(true);
//    resource_handler.setWelcomeFiles(new String[]{ "index.html" });
//
//    resource_handler.setBaseResource(Resource.newResource(resources));
//
//        HandlerCollection handlerCollection = new HandlerCollection();
//
//        handlerCollection.setHandlers(new Handler[] { wsHandler, context});
//
//        server.setHandler(handlerCollection);
//    //wsContainer.addEndpoint(AmpliboxWebSocketHandler.class);
//
////
////    List<Handler> handlersList = new ArrayList<Handler>();
////    handlersList.add(wsContainer);
////    handlersList.add(context);
////    server.setHandler(handlersList);
//
//
//     // wsContainer.start();
//
//
//
//    server.start();
//    server.join();
//  }

  private ServerEndpointConfig createEndpointConfig(Class<?> endpointClass) throws
                                                                            DeploymentException {
    ServerEndpoint annotation = endpointClass.getAnnotation(ServerEndpoint.class);
    if (annotation == null) {
      throw new InvalidWebSocketException("Unsupported WebSocket object, missing @" + ServerEndpoint.class + " annotation");
    }

    return ServerEndpointConfig.Builder.create(endpointClass, annotation.value())
        .subprotocols(Arrays.asList(annotation.subprotocols()))
        .decoders(Arrays.asList(annotation.decoders()))
        .encoders(Arrays.asList(annotation.encoders()))
        .configurator(configurator)
        .build();
  }


  private static List<ContainerInitializer> jspInitializers() {
    JettyJasperInitializer sci = new JettyJasperInitializer();
    ContainerInitializer initializer = new ContainerInitializer(sci, null);
    List<ContainerInitializer> initializers = new ArrayList<ContainerInitializer>();
    initializers.add(initializer);
    return initializers;
  }

  private static File getScratchDir() throws IOException {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

    if (!scratchDir.exists()) {
      if (!scratchDir.mkdirs()) {
        throw new IOException("Unable to create scratch directory: " + scratchDir);
      }
    }
    return scratchDir;
  }

  /**
   * JspStarter for embedded ServletContextHandlers
   *
   * This is added as a bean that is a jetty LifeCycle on the ServletContextHandler.
   * This bean's doStart method will be called as the ServletContextHandler starts,
   * and will call the ServletContainerInitializer for the jsp engine.
   *
   */
  public static class JspStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller
  {
    JettyJasperInitializer sci;
    ServletContextHandler context;

    public JspStarter (ServletContextHandler context)
    {
      this.sci = new JettyJasperInitializer();
      this.context = context;
      this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
    }

    @Override
    protected void doStart() throws Exception
    {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(context.getClassLoader());
      try
      {
        sci.onStartup(null, context.getServletContext());
        super.doStart();
      }
      finally
      {
        Thread.currentThread().setContextClassLoader(old);
      }
    }
  }

}
