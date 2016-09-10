package com.nirima.snowglobe.web.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import java.text.SimpleDateFormat;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
@Consumes(MediaType.WILDCARD) // NOTE: required to support "non-standard" JSON variants
@Produces(MediaType.WILDCARD)
public class MyJacksonProvider extends JacksonJaxbJsonProvider {
  private ObjectMapper mapper;

  public MyJacksonProvider(ObjectMapper mapper) {
    super(mapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
  }

  public MyJacksonProvider() {
    this(getMapper());
  }

  public static ObjectMapper getMapper() {
    final ObjectMapper mapper = new ObjectMapper();

    //mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  //  Hibernate4Module hbm = new Hibernate4ModuleExtra();
  //  hbm.configure(Hibernate4Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);

  //  mapper.registerModule(hbm);

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    mapper.setDateFormat(simpleDateFormat); // 1.8 and above

    return mapper;
  }

}