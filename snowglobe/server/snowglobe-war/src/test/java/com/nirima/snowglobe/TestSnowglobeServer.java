package com.nirima.snowglobe;


import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class TestSnowglobeServer {

  public int PORT = 8808;

  public Server server;

  public static void main(String[] args) {
    TestSnowglobeServer ts = new TestSnowglobeServer();

    try {
      ts.run();
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
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


  private void run() throws Exception {
    server = new Server();
    ServerConnector http = new ServerConnector(server,
                                               new HttpConnectionFactory());
    http.setPort(PORT);
    http.setIdleTimeout(30000);
    http.setHost("0.0.0.0");

    server.addConnector(http);

    System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");



    WebAppContext webapp = new WebAppContext();

    webapp.setContextPath("/");

    //wac.setDescriptor("WEB-INF/web.xml");
    File targetDir = new File(getClass().getResource("/").getFile()).getParentFile();
    File projectDir = targetDir.getParentFile();


    //System.out.println("ROOT = " + file);
    webapp.setResourceBase(new File(projectDir, "src/main/webapp").getAbsolutePath());
    webapp.setDescriptor("WEB-INF/web.xml");
    webapp.setParentLoaderPriority(true);



          /*
		 * All these configurations allow us to use things like Annotations
		 * JSP 3.1 (@Servlet not CDI, you need weld for that) and JNDI.
		 */
    webapp.setConfigurations(new Configuration[] {
        new AnnotationConfiguration(),
        new WebInfConfiguration(),
        new WebXmlConfiguration(),
        new MetaInfConfiguration(),
        new FragmentConfiguration(),
        new EnvConfiguration(),
        new PlusConfiguration(),
        new JettyWebXmlConfiguration()
    });

    webapp.setAttribute("javax.servlet.context.tempdir", getScratchDir());


    webapp.setAttribute(
        "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*\\.jar"
        //   //".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$"
        //
    );


          /*
		 * Configure the application to support the compilation of JSP files.
		 * We need a new class loader and some stuff so that Jetty can call the
		 * onStartup() methods as required.
		 */
    webapp.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
    webapp.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
    webapp.addBean(new ServletContainerInitializersStarter(webapp), true);
    webapp.setClassLoader(new URLClassLoader(new URL[0], TestSnowglobeServer.class.getClassLoader()));



    server.setStopAtShutdown(true);


    HandlerCollection handlerCollection = new HandlerCollection();

    handlerCollection.setHandlers(new Handler[] { webapp});

    // server.setHandler(wac);
    server.setHandler(handlerCollection);



    server.start();
    //
  }
}
