package com.nirima.snowglobe.test

import com.nirima.snowglobe.core.Module
import com.nirima.snowglobe.core.SGItem
import com.nirima.snowglobe.docker.DockerProvider

/**
 * Created by magnayn on 13/09/2016.
 */
@SGItem("test_provider")
public class TestDockerProvider extends DockerProvider {

    TestDockerProvider(Module module, String id,
                       Closure closure) {
        super(module, id, closure)
    }
}