package com.nirima.snowglobe.web.jersey;

import com.google.inject.Injector;

import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

import com.nirima.snowglobe.web.data.GlobeResource;
import com.nirima.snowglobe.web.data.ProgressManager;
import com.nirima.snowglobe.web.docker.DockerResource;
import com.nirima.snowglobe.web.site.SiteResource;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
//import org.hibernate.ejb.connection.InjectedDataSourceConnectionProvider;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.servlet.ServletContext;

/**
 * Created by magnayn on 04/11/14.
 */
public class JerseyApplication  extends ResourceConfig {


  @Inject
  public JerseyApplication(ServiceLocator serviceLocator, ServletContext servletContext) {
    packages("com.nirima.snowglobe.web.guice");
    property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/.*html");
    property(ServletProperties.FILTER_FORWARD_ON_404, true);



    register(GlobeResource.class);
    register(SiteResource.class);
    register(DockerResource.class);

    register(MultiPartFeature.class);

    //register(OldUserResource.class);

    property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
    property("jersey.config.server.headers.location.relative.resolution.rfc7231", true);

    System.out.println("Registering injectables...");

    GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

    GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
    //guiceBridge.bridgeGuiceInjector(getInjector());

    Injector i = (Injector) servletContext.getAttribute(Injector.class.getName());
    guiceBridge.bridgeGuiceInjector(i);
  }

}
