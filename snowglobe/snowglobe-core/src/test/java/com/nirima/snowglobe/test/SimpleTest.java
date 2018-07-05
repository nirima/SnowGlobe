package com.nirima.snowglobe.test;

import com.nirima.snowglobe.SGExec;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Created by magnayn on 01/09/2016.
 */
public class SimpleTest extends TestCase
 {
//  public void testOne() {
//    XStream xs = new XStream(new StaxDriver());
//
//
//
//    ChiaDSL dsl = new ChiaDSL();
//    //DiskState ds = (DiskState) xs.fromXML(getClass().getResourceAsStream("state.sgstate") );
//
//    //dsl.load(ds);
//
//    dsl.parseScript( new File(getClass().getResource("test.chia").getFile()) );
//    Object snowGlobe = dsl.runScript();
//
//    System.out.println(snowGlobe);
//
//    DependencyVisitor dependencyVisitor = new DependencyVisitor();
//    dsl.earth.accept(dependencyVisitor);
//
//    ChiaExecutionPlan cep = dsl.earth.getExecutionPlan();
//    System.out.println("===============");
//    System.out.println(cep);
//
//    Object result = cep.run();
//
//
//    String re = xs.toXML(result);
//    System.out.println(re);
//
//  }

  public void testOne() throws Exception {
    SGExec exec = new SGExec(new File(getClass().getResource("simple-docker-1.sg").getFile()));

    exec.graph(System.out);

  }

  public void testEnv() throws Exception {
    SGExec exec = new SGExec(new File(getClass().getResource("env.sg").getFile()));

    exec.graph(System.out);

  }

  public void testFunctions() throws Exception {
    SGExec exec = new SGExec(new File(getClass().getResource("functions.sg").getFile()));

    exec.apply();

  }

  public void testWithGroovy() throws Exception {
    SGExec exec = new SGExec(new File(getClass().getResource("groovy-inline.sg").getFile()));

    exec.apply();

  }

  public void testLoadFile() throws Exception {
    SGExec exec = new SGExec(new File(getClass().getResource("loadFile.sg").getFile()));

    exec.apply();

  }


  public void xxtestRealtime() throws Exception {
    SGExec exec = new SGExec(new File(getClass().getResource("realtime.sg").getFile()));


    exec.graph(System.out);


    exec.apply();
    String state = exec.save();
    System.out.println(state);

    // That which is created, must be DESTROYED

    exec = new SGExec(getClass().getResourceAsStream("realtime.sg"), new ByteArrayInputStream(state.getBytes()));
    exec.destroy();
    state = exec.save();
    System.out.println(state);

  }

  public void testAWS() throws Exception {
    SGExec exec = new SGExec(new File(getClass().getResource("/com/nirima/aws/aws1.sg").getFile()));

    exec.apply();
    String state = exec.save();
    System.out.println(state);


  }

  public void xxtestOne() throws InterruptedException {
   SGExec exec = new SGExec(new File(getClass().getResource("simple-docker.sg").getFile()),
                            new File(getClass().getResource("simple-docker.sgstate").getFile()));


    exec.graph(System.out);

    try {
      exec.apply();
    }
    finally {
      exec.save();
    }
//
//    SGWriter writer = new SGWriter(System.out);
//    writer.write(exec.snowGlobe);
//
//
//    exec = new SGExec(new InMemoryStateProvider(),
//                      new File(getClass().getResource("simple-docker.sg").getFile()) ,
//                      new File(getClass().getResource("simple-docker.sgstate").getFile()) );
//
//    exec.graph(System.out);

   // Thread.sleep(30000);

    //exec.destroy();

  }

  /*
  public void testTwo() {
    XStream xs = new XStream(new StaxDriver());



    SnowGlobeSystem dsl = new SnowGlobeSystem();
    //DiskState ds = (DiskState) xs.fromXML(getClass().getResourceAsStream("state.sgstate") );

    //dsl.load(ds);

    dsl.parseScript( new File(getClass().getResource("test-mp.sg").getFile()) );
    SGSnowGlobe snowGlobe = dsl.runScript();

    SnowGlobeContext ctxt = dsl.getState(snowGlobe);

    Graph g = new GraphBuilder().build(ctxt);
    String out = new GraphBuilder().graphViz(g);
    System.out.println(out);


    String state = xs.toXML(g);

    System.out.println(state);

    DependencyList_DFS list = new DependencyList_DFS();
    list.insert(g.getRootNode());

    list.getNodes().forEach( it -> System.out.println(it) );

    PlanBuilder pb = new PlanBuilder();

    Plan plan = pb.buildPlan(new Graph(new SGNode()), g);

    System.out.println(plan.describe());


  }
  */
}
