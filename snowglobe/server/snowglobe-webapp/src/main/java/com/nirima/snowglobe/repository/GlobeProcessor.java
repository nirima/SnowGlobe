package com.nirima.snowglobe.repository;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.SGParameters;
import com.nirima.snowglobe.web.data.Globe;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class GlobeProcessor implements IRepositoryModule {

  final String id;
  final FilesystemRepository repo;

  public GlobeProcessor(FilesystemRepository repo, String id) {
    this.id = id;
    this.repo = repo;
  }

  private File rootDir() {
    return new File(getRepositoryRoot(), id);
  }

  private File getRepositoryRoot() {
    return repo.getRepositoryRoot();
  }

  public Globe details() {
    Globe globe = new Globe();
    globe.id = id;
    for(File f : rootDir().listFiles()) {
      if( f.getName().endsWith(".sg") ) {
        globe.configFiles.add(f.getName());
      }
    }

    if( new File(rootDir(), ".git").exists() )
      globe.type = "git";

    File f = new File(getRepositoryRoot(), id + "/snowglobe.sgstate");
    if( f.exists() )
      globe.lastUpdate = new Date(f.lastModified());

    return globe;
  }

  @Override
  public IRepository getRepository() {
    return this.repo;
  }

  public void create() throws IOException {
    File f = new File(getRepositoryRoot(), id);
    f.mkdirs();
    setState("");
    setConfig(null, "snowglobe{}");
  }

  public String getState() throws IOException {
    File f = new File(getRepositoryRoot(), id + "/snowglobe.sgstate");
    if( !f.exists() )
      return "";
    return Files.toString(f, Charsets.UTF_8);
  }

  public void setState(String data) throws IOException {
    File f = new File(getRepositoryRoot(), id + "/snowglobe.sgstate");
    Files.write(data.getBytes(), f);
  }

  public String getConfig(String name) throws IOException {
    if(Strings.isNullOrEmpty(name))
      name = "snowglobe.sg";

    File f = new File(rootDir(), name);
    if( !f.exists() )
      return "";
    return Files.toString(f, Charsets.UTF_8);
  }

  public void setConfig(String name, String data) throws IOException {
    if(Strings.isNullOrEmpty(name))
      name = "snowglobe.sg";

    File f = new File(rootDir(), name);
    Files.write(data.getBytes(), f);
  }

  public SGExec getSGExec(SGParameters parameters) {

    try {
      SGExec
          sgExec =
          new SGExec(new File(getRepositoryRoot(), id + "/snowglobe.sg"),
                     new File(
                         getRepositoryRoot(), id + "/snowglobe.sgstate"), parameters);

      return sgExec;
    }
    catch(Exception ex) {
      ex.printStackTrace();
      throw Throwables.propagate(ex);
    }
  }

  public void delete() throws IOException {
    File path = new File(getRepositoryRoot(), id);
    
    FileUtils.deleteDirectory(path);
  }


}
