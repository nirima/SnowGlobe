package com.nirima.snowglobe.sinatra.utils;

import com.nirima.snowglobe.sinatra.model.Host;
import com.nirima.snowglobe.sinatra.model.Service;
import com.nirima.snowglobe.sinatra.model.Sinatra;

public class SSHConfigBuilder {
    private final Sinatra sinatra;

     public SSHConfigBuilder(Sinatra sinatra) {
       this.sinatra = sinatra;
     }

     public void build() {
       sinatra.findAll(Service.class).filter(svc -> svc.id.equals("ssh")).forEach(
           ssh -> {

             System.out.println("Host " + ssh.getFullId());
             System.out.println("  Hostname " + ((Host)ssh.parent).ip);

             Service svc = ssh.getVia();
             if( svc != null )
             {
               System.out.println("  ProxyCommand ssh " + svc.getFullId() + " -W %h:%p" );
             }

             System.out.println();

           }

       );
     }
}
