package com.nirima.snowglobe.repository;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

public class FilesystemRepositoryTest {

  @Test
  public void testCloneRepo() throws Exception {
    FilesystemRepository repo = new FilesystemRepository(null, new File("/tmp/fry"));
   // repo.cloneRepo("https://github.com/nirima/snowglobe-test-lib1");
    //repo.cloneRepo("https://github.com/AllocateSoftware/support", new C);
  }
}