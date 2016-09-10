package com.nirima.snowglobe.docker;


import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class DockerRegistryChecker {

  private static final Logger log = LoggerFactory.getLogger(DockerRegistryChecker.class);

  final Client client;
  private final String baseUrl;

  public static class Image {

    final String name;

    Image(String name) {
      this.name = name;
    }

    public boolean hasRemoteRepository() {
      return this.name.split("/")[0].contains(".");
    }


    String getImageName() {
      String n =  getImageId();
      int idx = n.indexOf(':');
      if (idx > 0) {
        n = n.substring(0, idx);
      }

      return n;
    }

    String getImageId() {
      // return foo.com/a:b --> a:b
      // return a/b:c --> a/b:c
      // return b:c --> library/b:c

      if( hasRemoteRepository() ) {
        return this.name.substring( this.name.indexOf("/")+1);
      }
      if (!this.name.contains("/")) {
        return "library/" + this.name;
      }

      return this.name;
    }

    String getTag() {
      int idx = name.indexOf(':');
      if (idx > 0) {
        return name.substring(idx + 1);
      }
      return "latest";
    }

    @Override
    public String toString() {
      return name;
    }

    public String getRepository() {
      return this.name.split("/")[0];
    }
  }

  public static class Builder {

    Image image;
    private String username, password;
    //private String
    private String authorizationHeaderValue;


    public Builder forImage(String img) {
      image = new Image(img);
      return this;
    }

    public Builder withAuth(String username, String password) {
      this.username = username;
      this.password=  password;

       this.authorizationHeaderValue =
        "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + password ).getBytes());

      return this;
    }

    public DockerRegistryChecker build() {
      if (image.hasRemoteRepository()) {
        return new DockerRegistryChecker("https://" + image.getRepository() + "/v2");
      } else {
        DockerRegistryChecker drc = new DockerRegistryChecker("https://registry-1.docker.io/v2");
        this.authorizationHeaderValue = "Bearer " + drc.getAuthId(image) ;
        return drc;
      }
    }

    public String getId() {
      return build().getImageId(image, authorizationHeaderValue);
    }

  }

  public static Builder builder() {
    return new Builder();
  }


  private DockerRegistryChecker(String baseUrl) {
    this.baseUrl = baseUrl;
    ClientConfig clientConfig = new ClientConfig();

    clientConfig.register(JacksonJsonProvider.class);

    client = ClientBuilder.newClient(clientConfig);
  }

  public String getImageId(Image id, String authorizationHeaderValue) {

    log.info("Getting image for {}", id);
    try {

      WebTarget webTarget
          = client.target(baseUrl);

      webTarget = webTarget.path(id.getImageName() + "/manifests/" + id.getTag());

      log.info("URL {}", webTarget);

      DockerManifestSummary
          result =
          webTarget
              .request("application/vnd.docker.distribution.manifest.v2+json")
         
              .header("Authorization", authorizationHeaderValue)

              .get(DockerManifestSummary.class);

      log.info("Image Id for {} = {}", id, result.config.digest);

      return result.config.digest;
    } catch (Exception ex) {
      log.info("Getting image failed for {}", id);
      throw ex;
    }
  }

  public String getAuthId(Image id) {

    WebTarget webTarget
        = client.target("https://auth.docker.io/token");

    webTarget = webTarget.queryParam("service", "registry.docker.io")
        .queryParam("scope", "repository:" + id.getImageName() + ":pull")
    ;

    DockerAuth result = webTarget.request(MediaType.APPLICATION_JSON_TYPE).get(DockerAuth.class);

    return result.token;
  }


}
