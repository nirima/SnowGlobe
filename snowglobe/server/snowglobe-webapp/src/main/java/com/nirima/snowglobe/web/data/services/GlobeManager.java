package com.nirima.snowglobe.web.data.services;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.environment.SnowglobeEnvironment;
import com.nirima.snowglobe.repository.FilesystemRepository;
import com.nirima.snowglobe.repository.IRepository;
import com.nirima.snowglobe.repository.IRepositoryModule;
import com.nirima.snowglobe.web.data.Globe;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobeManager {

  final SnowglobeEnvironment environment;
  final FilesystemRepository repository;

  public GlobeManager() {
    this.environment = SnowglobeEnvironment.build();
    String env = environment.getFileRepositoryRoot();
    this.repository = env==null?new FilesystemRepository():new FilesystemRepository(new File(env));
  }

//  File getRepositoryRoot() {
//    String env = environment.getFileRepositoryRoot();
//    if( env == null ) {
//      File r = new File(System.getProperty("user.home"));
//      return new File(r, ".snowglobe");
//    }
//    return new File(env);
//  }

  public Collection<Globe> list() {
    return repository.list();
  }



  public IRepositoryModule forGlobe(String id) {
    return repository.forGlobe(id);
  }


  public void clone(String id) throws GitAPIException, URISyntaxException {
    repository.cloneRepo(id);
  }
}
