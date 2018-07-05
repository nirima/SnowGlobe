package com.nirima.snowglobe.provisioner

import com.nirima.snowglobe.core.Provisioner
import com.nirima.snowglobe.core.Resource
import com.nirima.snowglobe.core.SGItem


@SGItem("remote-exec")
class RemoteExecProvisioner extends Provisioner{

    public String[] inline;

    @Override
    void run(Resource resource) {

    }
}
