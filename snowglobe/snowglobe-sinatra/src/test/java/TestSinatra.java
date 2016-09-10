import com.nirima.snowglobe.sinatra.model.Host;
import com.nirima.snowglobe.sinatra.model.Service;
import com.nirima.snowglobe.sinatra.model.Sinatra;
import com.nirima.snowglobe.sinatra.model.SinatraSystem;
import com.nirima.snowglobe.sinatra.utils.SSHConfigBuilder;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by magnayn on 27/04/2017.
 */
public class TestSinatra extends TestCase {
  public void testDSL() throws MalformedURLException {
    SinatraSystem ss = new SinatraSystem();
    ss.parseScript(new URL("file://test.dsl"), getClass().getResourceAsStream("/test.dsl"));
    Sinatra sval = ss.runScript();

    System.out.println(sval);

    sval.findAll(Service.class).forEach( svc ->
                                         {
                                           System.out.println(showAccessChain(svc));


                                         }
                                         
    );


    new SSHConfigBuilder(sval).build();



  }

  private String showAccessChain(Service svc) {
    String str = "[" + svc.getFullId() + "]" + ((Host)svc.parent).ip + ":" + svc.port;

    Service via = svc.getVia();
    if( via != null )
      str = str + " via " +showAccessChain(via);
    return str;
  }
}
