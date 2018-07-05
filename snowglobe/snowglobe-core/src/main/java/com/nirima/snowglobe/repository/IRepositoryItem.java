package com.nirima.snowglobe.repository;

import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.SGParameters;
import com.nirima.snowglobe.web.data.Globe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * API for accessing a repository's module.
 */
public interface IRepositoryItem {
  IRepository getRepository();

  void create() throws IOException;

  String getState() throws IOException;

  void setState(String data) throws IOException;

  String getConfig(String name) throws IOException;

  void setConfig(String name, String data) throws IOException;

  SGExec getSGExec();

  void delete() throws IOException;

  Globe details();

  String getVariables() throws IOException;

  void setVariables(String parameters) throws IOException;

  Set<String> getTags();

  void setTags(Set<String> tags);
}
