package com.nirima.snowglobe.repository;

import com.nirima.snowglobe.web.data.Globe;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FilesystemRepository implements IRepository {
  final File root;

  public FilesystemRepository() {

      File r = new File(System.getProperty("user.home"));
      this.root = new File(r, ".snowglobe");
    }

  public FilesystemRepository(File root) {
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

          File f = new File(dir,  "/snowglobe.sgstate");
          if( f.exists() )
            g.lastUpdate = new Date(f.lastModified());

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

  public void cloneRepo(String uri) throws GitAPIException, URISyntaxException {

    File location = getPathForUri(uri);

    CredentialsProvider cp = new UsernamePasswordCredentialsProvider("magnayn", "mbax3nrm");

    Git git = Git.cloneRepository()
        .setCredentialsProvider(cp)
        .setURI( uri )
        .setDirectory( location )
        .call();

  }

  @Override
  public IRepositoryModule forGlobe(String id) {
    return new GlobeProcessor(this, id.replace(":","/"));
  }


}
