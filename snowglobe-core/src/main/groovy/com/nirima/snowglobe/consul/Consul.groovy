package com.nirima.snowglobe.consul

import com.google.common.net.HostAndPort
import com.nirima.snowglobe.core.*
import com.nirima.snowglobe.plan.PlanAction
import com.orbitz.consul.Consul

/**
 * Created by magnayn on 04/09/2016.
 */
@SGItem("consul_provider")
class ConsulProvider extends Provider {

    public String address;

    ConsulProvider(Module module, String id, Closure closure) {
        super(module, id, closure)
    }

    public Consul getConsulClient() {

        Thread.sleep(1000);

        Consul consul = Consul.builder()
                .withHostAndPort(HostAndPort.fromString(address))
                .build();

        return consul;
    }
}

class ConsulKeyPrefixState extends ResourceState {
    String path_prefix;
    Map subkeys;

    ConsulKeyPrefixState(Resource parent, Closure closure) {
        super(parent, closure);
    }

    Closure getDefaults() {
        return {
            if(provider == null) {
                provider = consul_provider(null);
            }
        }
    }
}
@SGItem("consul_key_prefix")
class ConsulKeyPrefix extends Resource<ConsulKeyPrefixState> {


    ConsulKeyPrefix(Module module, String id, Closure closure) {
        super(module, id, closure)
    }


    public PlanAction assess() {
        return new ConsulKeyPrefixAction(this);
    }
}

class ConsulKeysStateKey implements Serializable {
    String path;
    String value;
}


class ConsulKeysState extends ResourceState {
    List<ConsulKeysStateKey> key = [];

    ConsulKeysState(Resource parent, Closure closure) {
        super(parent, closure);
    }

    public void key(Closure c) {
        ConsulKeysStateKey p = new ConsulKeysStateKey();
        c.delegate = p;
        c.resolveStrategy = Closure.DELEGATE_FIRST

        c()

        key << p;
    }

    @Override
    void accept(Object context) {

        key=[]

        super.accept(context)
    }

    Closure getDefaults() {
        return {
            if(provider == null) {
                provider = consul_provider(null);
            }
        }
    }
}
@SGItem("consul_keys")
class ConsulKeys extends Resource<ConsulKeysState> {


    ConsulKeys(Module module, String id, Closure closure) {
        super(module, id, closure)
    }

    public PlanAction assess() {
        return new ConsulKeysAction(this);
    }

}
