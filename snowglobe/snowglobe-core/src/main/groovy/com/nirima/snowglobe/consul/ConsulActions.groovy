package com.nirima.snowglobe.consul

import com.nirima.snowglobe.plan.PlanActionBase
import com.orbitz.consul.Consul
import com.orbitz.consul.KeyValueClient
import groovy.util.logging.Slf4j

@Slf4j
class ConsulKeyPrefixAction extends PlanActionBase<ConsulKeyPrefix, ConsulKeyPrefixState> {


    ConsulKeyPrefixAction(ConsulKeyPrefix nodePair) {
        super(nodePair)
    }


    @Override
    ConsulKeyPrefixState create(ConsulKeyPrefixState desiredState) {
        ConsulProvider dp = desiredState.getProvider();
        Consul client = dp.getConsulClient();
        KeyValueClient kvClient = client.keyValueClient();

        String prefix = desiredState.path_prefix;

        desiredState.subkeys.each {

            it ->
                String key = prefix + it.key;
                log.debug "Setting consul ${key} = ${it.value}"
                kvClient.putValue(key, it.value);
        }


        return desiredState;
    }

    @Override
    ConsulKeyPrefixState update(ConsulKeyPrefixState oldState, ConsulKeyPrefixState desiredState) {
        ConsulProvider dp = desiredState.getProvider();
        Consul client = dp.getConsulClient();
        KeyValueClient kvClient = client.keyValueClient();

        String prefix = desiredState.path_prefix;

        desiredState.subkeys.each {

            it ->
                String key = prefix + it.key;
                log.debug "Setting consul ${key} = ${it.value}"
                kvClient.putValue(key, it.value);
        }


        return desiredState;
    }

}

@Slf4j
class ConsulKeysAction extends PlanActionBase<ConsulKeys, ConsulKeysState> {


    ConsulKeysAction(ConsulKeys nodePair) {
        super(nodePair)
    }

    @Override
    ConsulKeysState create(ConsulKeysState desiredState) {
        ConsulProvider dp = desiredState.getProvider();
        Consul client = dp.getConsulClient();
        KeyValueClient kvClient = client.keyValueClient();

        desiredState.key.each {
            log.debug "Setting consul ${it.path} = ${it.value}"
            kvClient.putValue(it.path, it.value);
        }

        return desiredState
    }





}