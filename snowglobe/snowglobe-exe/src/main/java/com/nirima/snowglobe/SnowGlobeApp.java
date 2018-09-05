package com.nirima.snowglobe;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by magnayn on 06/09/2016.
 */
public class SnowGlobeApp {

  @Argument
  Action action;


  public static void main(String[] args) throws IOException {

    new SnowGlobeApp().doMain(args);
  }

  public void doMain(String[] args) throws IOException {
    CmdLineParser parser = new CmdLineParser(this);

    // if you have a wider console, you could increase the value;
    // here 80 is also the default
    parser.setUsageWidth(80);

    try {
      // parse the arguments.
      parser.parseArgument(args);

      // you can parse additional arguments if you want.
      // parser.parseArgument("more","args");

      // after parsing arguments, you should check
      // if enough arguments are given.
      //    if( arguments.isEmpty() )
      //    throw new CmdLineException(parser, "No argument is given");

    } catch (CmdLineException e) {
      // if there's a problem in the command line,
      // you'll get this exception. this will report
      // an error message.
      System.err.println(e.getMessage());
      System.err.println("snowglobe [options...] arguments...");
      // print the list of available options
      parser.printUsage(System.err);
      System.err.println();

      return;
    }

    // access non-option arguments
    System.err.println("other arguments are:");

    // Initialize
    File f1 = new File("snowglobe.sg");
    File f2 = new File("snowglobe.sgstate");

    SGExec exec = new SGExec(f1, f2);

    switch (action) {
      case list:
        

      case graph:
        exec.graph(System.out);
        break;
      case apply: {
        try {
          exec.apply();
        } finally {
          try (FileOutputStream fos = new FileOutputStream(f2)) {
            fos.write(exec.save().getBytes());
          }
        }
      }
      break;
      case destroy: {
        try {
          exec.destroy();
        } finally {
          try (FileOutputStream fos = new FileOutputStream(f2)) {
            fos.write(exec.save().getBytes());
          }
        }
      }
      break;
    }
  }
}
