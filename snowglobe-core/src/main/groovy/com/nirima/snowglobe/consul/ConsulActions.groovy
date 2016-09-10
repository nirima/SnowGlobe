package com.nirima.snowglobe.consul

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerCmd
import com.nirima.snowglobe.docker.DockerContainerState
import com.nirima.snowglobe.docker.DockerProvider
import com.nirima.snowglobe.plan.NodePair
import com.nirima.snowglobe.plan.PlanAction
import com.nirima.snowglobe.plan.PlanActionBase
import com.orbitz.consul.Consul
import com.orbitz.consul.KeyValueClient




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
                kvClient.putValue(key, it.value);
        }


        return desiredState;
    }

}

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
            kvClient.putValue(it.path, it.value);
        }

        return desiredState
    }



}