package com.nirima.snowglobe.web.data.services;

import com.nirima.snowglobe.environment.SnowglobeEnvironment;
import com.nirima.snowglobe.repository.Credentials;
import com.nirima.snowglobe.repository.FilesystemRepository;
import com.nirima.snowglobe.repository.IRepositoryItem;
import com.nirima.snowglobe.web.data.Globe;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.inject.Inject;

public class GlobeManager {

  final SnowglobeEnvironment environment;

  @Inject
  CredentialsManager credentialsManager;

  private FilesystemRepository repository;

  public GlobeManager() {
    this.environment = SnowglobeEnvironment.build();
  }

  public FilesystemRepository getRepository() {
    if( repository == null ) {
      String env = environment.getFileRepositoryRoot();
      this.repository = env==null?new FilesystemRepository(credentialsManager):new FilesystemRepository(credentialsManager, new File(env));
    }

    return repository;
  }

  public Collection<Globe> list() {
    return getRepository().list();
  }



  public IRepositoryItem forGlobe(String id) {
    return getRepository().forGlobe(id);
  }


  public void clone(String name, String url, Credentials credentials) throws GitAPIException, URISyntaxException {
    getRepository().cloneRepo(name, url, credentials);
  }
}
