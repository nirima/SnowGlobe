package com.nirima.snowglobe.repository;

public class RepositoryException extends RuntimeException {
  public RepositoryException(String name, Exception ex) {
    super(name,ex);
  }

  public RepositoryException(String s) {
    super(s);
  }

  public static RepositoryException namedRepositoryAlreadyExists(String name) {
    return new RepositoryException("The repository " + name + " already exists");
  }
}
