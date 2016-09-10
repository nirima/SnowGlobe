package com.nirima.relay;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by magnayn on 28/04/2017.
 */
public class RelayManager {

  Map<String, StartedProcess> runningMaps = new HashMap<>();

  public RelayManager() {

  }

  public int getPort(String remoteIp, int remotePort) {

    Relay relay = getRelayFor(remoteIp, remotePort);


    return 0;
  }

  private Relay getRelayFor(String remoteIp, int remotePort) {
    return null;
  }

  public StartedProcess launchRelay(String name) throws InterruptedException, TimeoutException, IOException {
    StartedProcess prc = new ProcessExecutor().command("/usr/bin/ssh", "bastion").start();
    return prc;
  }

}
