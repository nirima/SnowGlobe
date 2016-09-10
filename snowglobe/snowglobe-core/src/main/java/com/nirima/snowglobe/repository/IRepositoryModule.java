package com.nirima.snowglobe.repository;

import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.SGParameters;
import com.nirima.snowglobe.web.data.Globe;

import java.io.IOException;

/**
 * API for accessing a repository's module.
 */
public interface IRepositoryModule {
  IRepository getRepository();

  void create() throws IOException;

  String getState() throws IOException;

  void setState(String data) throws IOException;

  String getConfig(String name) throws IOException;

  void setConfig(String name, String data) throws IOException;

  SGExec getSGExec(SGParameters parameters);

  void delete() throws IOException;

  Globe details();
}
