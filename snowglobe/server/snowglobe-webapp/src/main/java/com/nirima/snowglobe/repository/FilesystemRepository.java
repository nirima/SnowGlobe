package com.nirima.snowglobe.repository;

import com.google.common.base.Strings;

import com.nirima.snowglobe.core.SnowGlobeSimpleReader;
import com.nirima.snowglobe.core.SnowGlobeSystem;

import com.nirima.snowglobe.web.data.Globe;
import com.nirima.snowglobe.web.data.services.CredentialsManager;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class FilesystemRepository implements IRepository {
  final File root;
  private CredentialsManager credentialsManager;

  public FilesystemRepository(CredentialsManager mgr) {
    this.credentialsManager = mgr;
      File r = new File(System.getProperty("user.home"));
      this.root = new File(r, ".snowglobe");
    }

  public FilesystemRepository(CredentialsManager mgr, File root) {
    this.credentialsManager = mgr;
    this.root = root;
  }

  public Collection<Globe> list() {
    return list("", root).stream().sorted(
        Comparator.comparing(o -> o.id)).collect(Collectors.toList());
  }

  protected File getRepositoryRoot() {
    return this.root;
  }

  private Collection<Globe> list(String prefix, File dir) {

    Set<Globe> gg = new HashSet<>();

    for(File file : dir.listFiles()) {
      if( file.isDirectory()) {
        String np = file.getName();
        if( prefix.length() > 0 )
          np = prefix + "/" + np;
        gg.addAll(list (np, file));
      } else {
        if( file.getName().endsWith(".sg")) {
          Globe g = new Globe();
          g.id = prefix.replace("/",":");
          g.name = dir.getName();
          g.description = dir.getName();


          try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            g.created = new Date(attr.creationTime().toMillis());
          } catch (IOException e) {
            // File system probably doesn't support this attribute.
          }


          File ft = new File(dir,  "/snowglobe.tags");
          if( ft.exists() ) {
            try {
              g.tags = FileUtils.readLines(ft,"UTF-8");
            } catch(IOException ex) {
              
            }
          }

          File f = new File(dir,  "/snowglobe.sgstate");
          if( f.exists() ) {
            g.lastUpdate = new Date(f.lastModified());

            // Determine if we have any state
            // TODO: we could totally cache this value - if not changed, answer is the same every time.
            SnowGlobeSystem snowGlobeSystem = new SnowGlobeSystem();
            try {
              SnowGlobeSimpleReader sgr = snowGlobeSystem.parseStateOnly(new FileInputStream(f));

              if( sgr == null || sgr.getResourceCount() == 0 ) {
                g.tags.add("_empty");
              }

            } catch (Exception e) {
              if(e instanceof FileNotFoundException) {
                // this one is OK
              } else {
                g.tags.add("_state_error");
              }
            }
          }

          // Determine if we are backed to a repository
          File fGit = new File(dir, ".git");
          if( fGit.exists() ) {
            g.type = "git";
          }


          gg.add(g);

        }
      }

    }

    return gg;

  }

  protected File getPathForUri(String uri) throws URISyntaxException {
    URI uriValue = new URI(uri);

    String host = uriValue.getHost();
    String path  = uriValue.getPath();

    String fullPath = host + path;

    return new File(getRepositoryRoot(), fullPath);
  }

  public void cloneRepo(String name, String uri, Credentials credentials) throws GitAPIException, URISyntaxException {

    File location;

    if( Strings.isNullOrEmpty(name) )
      location = getPathForUri(uri);
    else {
      location = new File(getRepositoryRoot(), name);
      if( location.exists() )
        throw RepositoryException.namedRepositoryAlreadyExists(name);
    }


    CloneCommand git = Git.cloneRepository()
        .setURI( uri )
        .setDirectory( location );

    if( credentials != null) {
      CredentialsProvider cp = credentials.getCredentialsProvider();
      git.setCredentialsProvider(cp);
    }


    git.call();

    try {
      storeCredentials(location, credentials);
    } catch (IOException e) {
      throw new RepositoryException("Could not save credentials", e);
    }

  }

  private void storeCredentials(File location, Credentials credentials)
      throws IOException {
    File credentialsFile = new File(location, ".repo");
    Properties p = new Properties();
    p.setProperty("username", credentials.getUsername());
    String passwordCrypt = Secret.encrypt(credentials.getPassword(), key).toString();
    p.setProperty("password", passwordCrypt);

    FileOutputStream fos = new FileOutputStream(credentialsFile);
    p.store(fos, "");
    fos.close();
  }

  private Credentials loadCredentials(File location)
      throws IOException {

    File credentialsFile = new File(location, ".repo");
    Properties p = new Properties();
    FileInputStream fis = new FileInputStream(credentialsFile);

    p.load(fis);
    fis.close();

    String passwordCrypt = p.getProperty("password");
    String password = new String(Secret.fromCryptedString(passwordCrypt).decrypt(key));

    return new Credentials(p.getProperty("username"), password);

  }

  // TODO: Per user decrypted by auth.
  private static final Secret.SKey key = new Secret.SKey("N9WuZpF76AEUzTmwiJO98A==");

  @Override
  public IRepositoryItem forGlobe(String id) {
    return new RepositoryItem(this, id.replace(":", "/"));
  }



  public CredentialsManager getCredentialsManager() {
    return credentialsManager;
  }

  public void setCredentialsManager(
      CredentialsManager credentialsManager) {
    this.credentialsManager = credentialsManager;
  }
}
