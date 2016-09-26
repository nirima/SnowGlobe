package com.nirima.snowglobe.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageCmd
import com.github.dockerjava.api.model.AuthConfig
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.command.PullImageResultCallback
import com.google.common.base.Objects
import com.nirima.snowglobe.core.*
import com.nirima.snowglobe.plan.PlanAction

/**
 * Created by magnayn on 04/09/2016.
 */

class DockerContainerPort {
    int internal;
    int external;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("internal", internal)
                .add("external", external)
                .toString();
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        DockerContainerPort that = (DockerContainerPort) o

        if (external != that.external) {
            return false
        }
        if (internal != that.internal) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = internal
        result = 31 * result + external
        return result
    }
}

class DockerContainerVolume {
    String from_container;

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        DockerContainerVolume that = (DockerContainerVolume) o

        if (from_container != that.from_container) {
            return false
        }

        return true
    }

    int hashCode() {
        return (from_container != null ? from_container.hashCode() : 0)
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("from_container", from_container)
                .toString();
    }
}

public class DockerContainerState extends ResourceState implements Comparable<DockerContainerState> {
    public String name;
    public String image;
    public List links;

    public List<DockerContainerPort> ports = [];
    public List<DockerContainerVolume> volumes = [];
    public List command;
    public List env;

    public String restart;

    public boolean publish_all_ports;
    public boolean must_run = true;

    public String id;

    DockerContainerState(Resource parent, Closure closure) {
        super(parent, closure);
    }

    public void ports(Closure c) {
        DockerContainerPort p = new DockerContainerPort();
        c.delegate = p;
        c.resolveStrategy = Closure.DELEGATE_FIRST

        c()

        ports<< p;
    }

    public void volumes(Closure c) {
        DockerContainerVolume p = new DockerContainerVolume();
        c.delegate = p;
        c.resolveStrategy = Closure.DELEGATE_FIRST

        c()

        volumes << p;
    }

    @Override
    void accept(Object context) {

        ports = []
        volumes = []

        super.accept(context)
    }

    Closure getDefaults() {
        Closure defaults = {
            if( provider == null )
                provider = docker_provider(null);
        }
        return defaults;
    }

    @Override
    int compareTo(DockerContainerState o) {

        if (this.is(o)) {
            return 0
        }
        if (getClass() != o.class) {
            return -1
        }

        DockerContainerState that = (DockerContainerState) o

        if (must_run != that.must_run) {
            return -1
        }
        if (publish_all_ports != that.publish_all_ports) {
            return -1
        }
        if (command != that.command) {
            return -1
        }
        if (env != that.env) {
            return -1
        }

        // Ignore Id as this is the actual container.

        if (image != that.image) {
            return -1
        }
        if (links != that.links) {
            return -1
        }
        if (name != that.name) {
            return -1
        }
        if (ports != that.ports) {
            return -1
        }
        if (restart != that.restart) {
            return -1
        }
        if (volumes != that.volumes) {
            return -1
        }

        return 0
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (image != null ? image.hashCode() : 0)
        result = 31 * result + (links != null ? links.hashCode() : 0)
        result = 31 * result + (ports != null ? ports.hashCode() : 0)
        result = 31 * result + (volumes != null ? volumes.hashCode() : 0)
        result = 31 * result + (command != null ? command.hashCode() : 0)
        result = 31 * result + (env != null ? env.hashCode() : 0)
        result = 31 * result + (restart != null ? restart.hashCode() : 0)
        result = 31 * result + (publish_all_ports ? 1 : 0)
        result = 31 * result + (must_run ? 1 : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        return result
    }
}

@SGItem("docker_container")
public class DockerContainer extends Resource<DockerContainerState> {

    DockerContainer(Module module, String id,
                    Closure closure) {
        super(module, id, closure)
    }


    public PlanAction assess() {
        return new DockerContainerAction(this);
    }

}



public class DockerImageState extends ResourceState {
    String name;

    String latest;

    String imageId;

    boolean keep_locally;

    public Object registry_provider;

    DockerImageState(Resource parent, Closure closure) {
        super(parent, closure);
    }

    Closure getDefaults() {
        Closure defaults = {
            if( provider == null )
                provider = docker_provider(null);
        }
        return defaults;
    }
}

@SGItem("docker_image")
public class DockerImage extends Resource<DockerImageState> {


    DockerImage(Module module, String id,
                Closure closure) {
        super(module, id, closure)
    }


    Closure getDefaults() {
        return {
            if(provider == null) {
                provider = docker_provider(null);
            }
            if( name.contains("/") && registry_provider == null ) {
                registry_provider = docker_registry(getRegistry())
            }
        }
    }

    public String getRegistry() {
        if( !name.contains("/") )
            return null;
        return name.substring(0,name.indexOf("/"));
    }

    public PlanAction assess() {
       return new DockerImageAction(this);
    }

}

@SGItem("docker_registry_image")
public class DockerRegistryImage extends DataSource<DockerRegistryImageState> {

    DockerRegistryImage(Module module, String id, Closure closure) {
        super(module, id, closure)
    }

    public String getSha256_digest() {

        DockerProvider dp = state.getProvider();
        DockerClient dc = dp.getDockerClient();
        PullImageCmd cmd = dc.pullImageCmd(state.name);


        // TODO: Figure this out
        if( state.name.startsWith("registry")) {
            AuthConfig ac = new AuthConfig();
            ac.withUsername("admin");
            ac.withPassword("admin123");

            cmd.withAuthConfig(ac);
        }

        PullImageResultCallback pullImageResultCallback = cmd.
                exec(PullImageResultCallback.newInstance());

        pullImageResultCallback.awaitSuccess();

        List<Image> imgs = dc.listImagesCmd().withImageNameFilter(state.name).exec();
        if( imgs.size() != 1 )
        {
            throw new IllegalStateException("Image ${state.name} is ambiguous or empty");
        }

        return imgs.get(0).id;


    }


}

public class DockerRegistryImageState extends DataSourceState {
    String name;

    DockerRegistryImageState(DataSource parent, Closure closure) {
        super(parent, closure)
    }
    Closure getDefaults() {
        Closure defaults = {
            if( provider == null )
                provider = docker_provider(null);
        }
        return defaults;
    }
//    public String getSha256_digest() {
//        // Do a pull
//    }
}

@SGItem("docker_provider")
public class DockerProvider extends Provider {
    public String host;

    DockerProvider(Module module, String id, Closure closure) {
        super(module, id, closure)
    }

    public DockerClient getDockerClient() {
        DefaultDockerClientConfig defaultDockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
        //.withDockerTlsVerify(true)
        //.withDockerCertPath("/Users/magnayn/.sdc/docker/jenkins")
                .build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(defaultDockerClientConfig).build();

        return dockerClient;
    }
}


@SGItem("docker_registry")
public class DockerRegistry extends Provider {
    public String username;
    public String password;

    DockerRegistry(Module module, String id, Closure closure) {
        super(module, id, closure)
    }

    public AuthConfig getAuthConfig() {
        AuthConfig ac = new AuthConfig();
        ac.withUsername(username);
        ac.withPassword(password);
        return ac;
    }
}

