package com.nirima.jenkins.plugins.snowglobe.registry;

import com.nirima.jenkins.plugins.snowglobe.action.SnowGlobeAction;
import com.nirima.jenkins.plugins.snowglobe.calls.SnowGlobeData;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import hudson.Extension;
import hudson.model.Run;
import jenkins.model.GlobalConfiguration;

/**
 * Created by magnayn on 12/09/2016.
 */
@Extension
public class SnowGlobeRegistry extends GlobalConfiguration implements Serializable {

  Map<String, SnowGlobeData> globes = new LinkedHashMap<>();

  Map<String, Set<String>> runToIds = new HashMap<>();


  public SnowGlobeRegistry() {
    load();
  }

  /**
   * Returns this singleton instance.
   *
   * @return the singleton.
   */
  public static SnowGlobeRegistry get() {
    return GlobalConfiguration.all().get(SnowGlobeRegistry.class);
  }

  public SnowGlobeData register(String id, SnowGlobeData data) {
    globes.put(id, data);
    save();
    return data;
  }

  public void register(String id, SnowGlobeData data, Run<?,?> run) throws IOException {
    String runId = makeRunId(run);
    register(id,data);

    SnowGlobeAction action = run.getAction(SnowGlobeAction.class);
    if (action == null) {
      action = new SnowGlobeAction(runId);
      run.addAction(action);
    }
    run.save();

    if( !runToIds.containsKey(runId)) {
      runToIds.put(runId, new HashSet<String>());
    }

    runToIds.get(runId).add(id);

    save();
  }

  public static String makeRunId(Run<?,?> run) {
    return run.getParent().getUrl() + run.getNumber();
  }

  public void remove(String id) {
    globes.remove(id);
    save();
  }

  public SnowGlobeData getById(String id) {
    SnowGlobeData data = globes.get(id);
    return data;
  }

  public Collection<SnowGlobeData> getActiveGlobes() {
    Set<SnowGlobeData> data = new HashSet<>();
    data.addAll(globes.values());

    data.stream().filter( it -> it.state != null );

    return data;
  }

  public void removeFromRun(Run<?,?> r) {

    String runId = makeRunId(r);

    Set<String> items = runToIds.get( runId );
    items.forEach( it -> remove(it) );
    runToIds.remove( runId );


  }

  public Set<String> getGlobesForRunId(String id) {
    return runToIds.get(id);
  }
}


