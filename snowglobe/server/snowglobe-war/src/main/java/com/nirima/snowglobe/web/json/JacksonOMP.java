package com.nirima.snowglobe.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
@Produces
public class JacksonOMP implements ContextResolver<ObjectMapper> {

  public JacksonOMP() {
    System.out.println("OMP");
  }

  @Override
  public ObjectMapper getContext(Class<?> aClass) {
    final ObjectMapper mapper = new ObjectMapper();
   // Hibernate4Module hbm = new Hibernate4ModuleExtra();
   // hbm.configure(Hibernate4Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);

   //mapper.registerModule(hbm);
    return mapper;
  }
}
