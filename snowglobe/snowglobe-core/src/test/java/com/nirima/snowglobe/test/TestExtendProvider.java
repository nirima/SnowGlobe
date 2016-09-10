package com.nirima.snowglobe.test;

import com.nirima.snowglobe.SGExec;
import com.nirima.snowglobe.core.SGItem;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Created by magnayn on 13/09/2016.
 */

public class TestExtendProvider  extends TestCase {


  public void testOne() throws Exception {
    SGExec exec = new SGExec(new File(getClass().getResource("provider.sg").getFile()));

    exec.graph(System.out);

  }


}
