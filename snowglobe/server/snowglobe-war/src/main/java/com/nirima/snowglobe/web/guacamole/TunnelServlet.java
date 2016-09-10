package com.nirima.snowglobe.web.guacamole;

import com.google.common.base.Throwables;

import com.nirima.snowglobe.sinatra.model.Host;
import com.nirima.snowglobe.sinatra.model.Service;
import com.nirima.snowglobe.sinatra.model.Sinatra;
import com.nirima.snowglobe.web.data.services.SinatraManager;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;


public class TunnelServlet extends GuacamoleHTTPTunnelServlet {

  @Inject
  SinatraManager sinatraManager;

  @Override
  protected GuacamoleTunnel doConnect(HttpServletRequest httpServletRequest)
      throws GuacamoleException {


    String req = httpServletRequest.getRequestURL().toString();
    String sID = req.substring(req.lastIndexOf("/") + 1);

    Service service = null;
    try {
      service = sinatraManager.getService(sID);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      Throwables.propagate(e);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Throwables.propagate(e);
    }

    // guacd connection information
    String hostname = "localhost";
    int port = 32799;

    // VNC connection information
    GuacamoleConfiguration config = new GuacamoleConfiguration();

    Host theHost = ((Host)service.parent);

    config.setProtocol(service.id);
    config.setParameter("hostname", theHost.ip);

    // Connect to guacd, proxying a connection to the VNC server above
    GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
        new InetGuacamoleSocket(hostname, port),
        config
    );

    // Create tunnel from now-configured socket
    GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
    return tunnel;

  }
}
