package com.nirima.snowglobe.web;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.nirima.snowglobe.environment.SnowglobeEnvironment;
import com.nirima.snowglobe.web.data.ProgressManager;
import com.nirima.snowglobe.web.data.services.CredentialsManager;
import com.nirima.snowglobe.web.data.services.GlobeManager;
import com.nirima.snowglobe.web.data.services.SinatraManager;
import com.nirima.snowglobe.web.guacamole.TunnelServlet;
import com.nirima.snowglobe.web.json.JacksonOMP;
import com.thetransactioncompany.cors.CORSFilter;

import org.glassfish.jersey.servlet.ServletContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.ws.rs.Produces;

//import org.hibernate.ejb.connection.InjectedDataSourceConnectionProvider;


public class GuiceServletConfig extends GuiceServletContextListener {


  private static Injector INJECTOR;
  private static List<Module> MODULES = Lists.newArrayList();
  private static Module allModulesAsArray;


  public static Injector getInjectorInstance() {
    return INJECTOR;
  }

  public static List<Module> getAllModules() {
    return MODULES;
  }

  public static Module[] getAllModulesAsArray() {
    return MODULES.toArray(new Module[MODULES.size()]);
  }
  

  @Provides
  @Singleton
  @Produces
  public JacksonJsonProvider createJacksonJsonProvider() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(
        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    return new JacksonJsonProvider(objectMapper);
  }

  @Override
  protected Injector getInjector() {
    return getInjectorInstance();
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    createInjectorInstance(servletContextEvent.getServletContext());
    super.contextInitialized(servletContextEvent);
  }

  public static Injector createInjectorInstance(ServletContext servletContext) {

    System.out.println("************* START");



    SnowglobeEnvironment environment = SnowglobeEnvironment.build();


    //Injector injector = Guice.createInjector(
  //  MODULES.add(
  //      new DBModule(environment));

  /*  MODULES.add(new AbstractModule() {
      @Override
      protected void configure() {
        bind(EntityQueryManager.class).asEagerSingleton();
        bind(IUserService.class).to(UserService.class).asEagerSingleton();
        bind(DataService.class).asEagerSingleton();
      }
    });
    */
  
    MODULES.add(
        new ServletModule() {
          @Override
          protected void configureServlets() {



            //bind(DataSource.class).to(jdbcDriver.class);
            //
            //   bind(ITestDataMaker.class).to(TestDataMaker.class);







                /* bind the REST resources */
            bind(JacksonOMP.class).asEagerSingleton();

    //        bind(DataResource.class).asEagerSingleton();
      //      bind(OldUserResource.class).asEagerSingleton();





            bind(CORSFilter.class).asEagerSingleton();

            //bind(PackageServeServlet.class).asEagerSingleton();
            //bind(PackageRedirectServlet.class).asEagerSingleton();

            //bind(EntityResource.class);

            //bind(ITestDataMaker.class).to(TestDataMaker.class);

            Map<String, String> initParams = new HashMap<String, String>();
            initParams.put("com.sun.jersey.config.feature.Trace",
                           "true");
            initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");


            Map<String, String> props = new HashMap<String, String>();
     //       props.put("javax.ws.rs.Application", Application.class.getName());
            props.put("jersey.config.server.wadl.disableWadl", "true");

            //serve("/services/*").with(ServletContainer.class, props);


            //this.objectMapper.setPropertyNamingStrategy(
            //        );



         /*   if( environment.security() ) {
              //install();

              filter("/*").through(GuiceShiroFilter.class);
            }
           */
            Map<String, String> params = new HashMap<String, String>();
            params.put("cors.supportedMethods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

            filter("/*").through(CORSFilter.class, params);

            bind(ServletContainer.class).asEagerSingleton();


            bind(ProgressManager.class).asEagerSingleton();
            bind(SinatraManager.class).asEagerSingleton();
            bind(GlobeManager.class).asEagerSingleton();
            bind(CredentialsManager.class).asEagerSingleton();

            bind(TunnelServlet.class).asEagerSingleton();
              serve("/tunnel/*").with(TunnelServlet.class);


          }


        });

 //   MODULES.add(new ShiroSecurityModule(servletContext));

    // MODULES.add(new BrandModule());

    INJECTOR = Guice.createInjector(MODULES);

   // INJECTOR.getInstance(ApplicationInitializer.class);

    servletContext.setAttribute(Injector.class.getName(),INJECTOR);

//    try {
//
//     /* if( environment.createTestData() ) {
//
//        INJECTOR.getInstance(ITestDataMaker.class).create();
//      }*/
//    } catch (IOException e) {
//      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//    }

     return INJECTOR;
  }
}