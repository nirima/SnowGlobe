package com.nirima.snowglobe.web.data;

import com.nirima.snowglobe.sinatra.model.Environment;
import com.nirima.snowglobe.sinatra.model.Host;
import com.nirima.snowglobe.sinatra.model.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by magnayn on 27/04/2017.
 */
public class EnvData implements Serializable {

  public String fullId;

  public List<HostData> hosts = new ArrayList<>();

  public static EnvData buildFromEnvironment(Environment e) {
    EnvData ed = new EnvData();

    ed.fullId = e.getFullId();

    e.getChildren().stream().forEach(
        h -> {
          ed.hosts.add(HostData.buildFromHost((Host)h));
        }
    );
    return ed;
  }

  public static class HostData
  {
    public List<HostService> services = new ArrayList<>();
    public String ip;
    public String fullId;

    public static HostData buildFromHost(Host host) {
      HostData hd = new HostData();

      hd.ip = host.ip;
      hd.fullId = host.getFullId();

      host.getChildren().stream().forEach(
          s -> {
            hd.services.add(HostService.buildFromService((Service)s));
          }
      );

      return hd;
    }
  }

  public static class HostService {
    public String id;
    public String fullId;
    public String type;

    public static HostService buildFromService(Service s) {
      HostService hs = new HostService();

      hs.type = s.type;
      hs.id = s.id;
      hs.fullId = s.getFullId();

      return hs;
    }
  }

}
