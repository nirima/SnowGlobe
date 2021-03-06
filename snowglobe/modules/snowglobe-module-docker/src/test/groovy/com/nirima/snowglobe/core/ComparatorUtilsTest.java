package com.nirima.snowglobe.core;

import com.nirima.snowglobe.docker.DockerContainerPort;
import com.nirima.snowglobe.docker.DockerContainerState;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ComparatorUtilsTest {

  @Test
  public void fieldwiseCompare() {
    DockerContainerState one = new DockerContainerState(null,null);
    DockerContainerState two = new DockerContainerState(null,null);

    Assert.assertEquals(ComparatorUtils.fieldwiseCompare(one, two), 0) ;
    assertEquals( one.compareTo(two), 0);

    one.name = "foo bar";
    Assert.assertNotEquals(ComparatorUtils.fieldwiseCompare(one, two), 0) ;
    assertNotEquals( one.compareTo(two), 0);
  }

  @Test
  public void comparePorts() {
    DockerContainerPort one = new DockerContainerPort();
    one.setInternal(1234);
    one.setExternal(5678);

    DockerContainerPort two = new DockerContainerPort();
    two.setInternal(1234);
    two.setExternal(5678);
    
    Assert.assertEquals(ComparatorUtils.fieldwiseCompare(one, two), 0) ;
    assertEquals( one.compareTo(two), 0);


  }

  @Test
  public void fieldwiseCompareList() {
    DockerContainerState one = new DockerContainerState(null,null);
    DockerContainerState two = new DockerContainerState(null,null);

    Assert.assertEquals(ComparatorUtils.fieldwiseCompare(one, two), 0) ;
    assertEquals( one.compareTo(two), 0);


    DockerContainerPort p = new DockerContainerPort();
    p.setInternal(1234);
    p.setExternal(5678);


    one.ports.add(p);
    Assert.assertNotEquals(ComparatorUtils.fieldwiseCompare(one, two), 0) ;
    assertNotEquals( one.compareTo(two), 0);


    DockerContainerPort p2 = new DockerContainerPort();
    p2.setInternal(1234);
    p2.setExternal(5678);

    two.ports.add(p2);

    Assert.assertEquals(ComparatorUtils.fieldwiseCompare(one, two), 0) ;
    assertEquals( one.compareTo(two), 0);



  }


}