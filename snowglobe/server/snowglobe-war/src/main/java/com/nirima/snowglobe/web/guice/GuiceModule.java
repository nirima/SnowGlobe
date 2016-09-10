package com.nirima.snowglobe.web.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;

/**
 * Created by magnayn on 04/11/14.
 */
public class GuiceModule extends AbstractModule {
  @Override
  protected void configure() {

  }

//  @Provides
//  @Singleton
//  @Produces
//  public JacksonJsonProvider createJacksonJsonProvider() {
//    ObjectMapper objectMapper = new ObjectMapper();
//    objectMapper.setPropertyNamingStrategy(
//        PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
//    return new JacksonJsonProvider(objectMapper);
//  }

  @Provides @Singleton
  ObjectMapper objectMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    return mapper;
  }

  @Provides @Singleton
  JacksonJsonProvider jacksonJsonProvider(ObjectMapper mapper) {
    return new MyJacksonProvider(mapper);
  }

//  @Provides
//  @Singleton
//  EntityQueryManager entityQueryManager() {
//    return new EntityQueryManager();
//  }
}
