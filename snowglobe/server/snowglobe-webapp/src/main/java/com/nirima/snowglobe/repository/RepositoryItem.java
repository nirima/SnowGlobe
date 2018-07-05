package com.nirima.snowglobe.repository;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.SGParameters;
import com.nirima.snowglobe.SGTags;
import com.nirima.snowglobe.web.data.Globe;
import com.nirima.snowglobe.web.data.services.CredentialsManager;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.javers.common.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an item in the SnowGlobe repository.
 */
public class RepositoryItem implements IRepositoryItem, ITransactionalRepositoryItem {

  private static final Logger log = LoggerFactory.getLogger(RepositoryItem.class);

  final String id;
  final FilesystemRepository repo;

  public RepositoryItem(FilesystemRepository repo, String id) {
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
    for (File f : rootDir().listFiles()) {
      if (f.getName().endsWith(".sg")) {
        globe.configFiles.add(f.getName());
      }
    }

    if (new File(rootDir(), ".git").exists()) {
      globe.type = "git";
    }

    File f = new File(getRepositoryRoot(), id + "/snowglobe.sgstate");
    if (f.exists()) {
      globe.lastUpdate = new Date(f.lastModified());
    }

    return globe;
  }

  @Override
  public String getVariables() throws IOException {

    File f = new File(getRepositoryRoot(), id + "/snowglobe.vars");
    if (!f.exists()) {
      return "";
    }

    return Files.toString(f, Charsets.UTF_8);

  }

  @Override
  public void setVariables(String data) throws IOException {
    File f = new File(getRepositoryRoot(), id + "/snowglobe.vars");
    Files.write(data.getBytes(), f);
  }

  @Override
  public Set<String> getTags() {
    File f = new File(getRepositoryRoot(), id + "/snowglobe.tags");
    try {
      if( f.exists() ) {
        List<String> lines = FileUtils.readLines(f, "UTF-8");
        return Sets.asSet(lines);
      }
    } catch (Exception e) {
      log.error("Error reading tags", e);
    }
    return new HashSet<>();
  }

  @Override
  public void setTags(Set<String> tags) {
    File f = new File(getRepositoryRoot(), id + "/snowglobe.tags");
    try {
      FileUtils.writeLines(f, "UTF-8", tags);
    } catch (IOException e) {
      log.error("Error writing tags");
    }
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
    if (!f.exists()) {
      return "";
    }
    return Files.toString(f, Charsets.UTF_8);
  }

  public void setState(String data) throws IOException {
    File f = new File(getRepositoryRoot(), id + "/snowglobe.sgstate");
    Files.write(data.getBytes(), f);
  }

  public String getConfig(String name) throws IOException {
    if (Strings.isNullOrEmpty(name)) {
      name = "snowglobe.sg";
    }

    File f = new File(rootDir(), name);
    if (!f.exists()) {
      return "";
    }
    return Files.toString(f, Charsets.UTF_8);
  }

  public void setConfig(String name, String data) throws IOException {
    if (Strings.isNullOrEmpty(name)) {
      name = "snowglobe.sg";
    }

    File f = new File(rootDir(), name);
    Files.write(data.getBytes(), f);
  }

  public SGParameters getSGParameters() throws IOException {
    SGParameters parameters = new SGParameters();
    parameters.load(new ByteArrayInputStream(getVariables().getBytes()));
    parameters.tags = new SGTags(this);
    return parameters;
  }

  public SGExec getSGExec() {

    try {
      SGExec
          sgExec =
          new SGExec(new File(getRepositoryRoot(), id + "/snowglobe.sg"),
                     new File(
                         getRepositoryRoot(), id + "/snowglobe.sgstate"), getSGParameters());

      return sgExec;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw Throwables.propagate(ex);
    }
  }

  public void delete() throws IOException {
    File path = new File(getRepositoryRoot(), id);

    FileUtils.deleteDirectory(path);
  }

  @Override
  public void begin() {
    // Update from repo
    Git git = null;
    try {
      
      git = openGit();
      if( git == null )
        return;

      git.pull().call();
    }
    catch(InvalidConfigurationException e) {
      // Fine
    }
    catch (Exception ex) {
      //throw new RepositoryException("Error", ex);
      // Probably not found
    }

  }

  @Override
  public void commit(String message) {
    try {

      CredentialsManager mgr = repo.getCredentialsManager();

      Git git = openGit();
      if( git == null )
        return;


      // Add any files.
      git.add().addFilepattern(".").call();

      git.commit().setMessage(message)
          .setCommitter("SnowGlobe User", "snowglobe@nirima.com")
          .setAuthor("SnowGlobe User", "snowglobe@nirima.com")
          .setAllowEmpty(true)
          .call();


      if( git.remoteList().call().size() > 0 ) {
        Credentials c = mgr.getCredentialsForLocation(new URL("http://github.com"));

        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(c.getUsername(), c.getPassword());

        Iterable<PushResult> results = git.push().setCredentialsProvider(cp).call();

        results.forEach( pr -> log.info(pr.getMessages() ));
      }

    } catch (Exception ex) {
      ex.printStackTrace();
      //throw new RepositoryException("Error", ex);
    }
  }

  private Git openGit() {

    File directory = rootDir();

    while( !directory.equals(getRepositoryRoot())) {
      // Use .gitmask to mask the entire directory
      if (new File(directory, ".gitmask").exists())
        return null;

      try {
        Git g = Git.open(directory);
        return g;
      } catch(Exception ex) {

      }
      directory = directory.getParentFile();
    }
    return null;
  }


}
