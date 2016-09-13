package com.nirima.snowglobe.jenkins

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.snowglobe.core.Module
import com.nirima.snowglobe.core.Provider
import com.nirima.snowglobe.core.SGItem
import com.nirima.snowglobe.docker.DockerProvider

@SGItem("jenkins_docker_provider")
public class JenkinsDockerProvider extends DockerProvider {
    public String cloudName;

    JenkinsDockerProvider(Module module, String id, Closure closure) {
        super(module, id, closure)
    }

    public DockerClient getDockerClient() {

        def dc = jenkins.model.Jenkins.getInstance().getCloud(cloudName);

        return dc.getClient();
    }
}