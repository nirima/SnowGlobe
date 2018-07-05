package com.nirima.snowglobe.provisioner

import com.nirima.snowglobe.core.Provisioner
import com.nirima.snowglobe.core.Resource
import com.nirima.snowglobe.core.SGItem

@SGItem("file")
class FileProvisioner extends Provisioner{

    public String source;
    public String destination;

    @Override
    void run(Resource resource) {

    }
}

