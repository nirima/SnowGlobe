package com.nirima.snowglobe.docker

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageCmd
import com.github.dockerjava.api.model.AuthConfig
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.PullResponseItem
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.command.PullImageResultCallback
import com.google.common.base.Objects
import com.google.common.base.Strings
import com.nirima.snowglobe.core.*
import com.nirima.snowglobe.plan.PlanAction
import com.nirima.snowglobe.utils.ThreadLog
import com.nirima.snowglobe.utils.ThreadLogBase
import groovy.util.logging.Slf4j

/**
 * Created by magnayn on 04/09/2016.
 */

class DockerContainerPort implements Comparable {

    /**
     * Port inside the container
     */
    public int internal;

    /**
     * Port to map to externally
     */
    public int external;

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

    int compareTo(Object o) {
        return ComparatorUtils.fieldwiseCompare(this, o);
    }

    int hashCode() {
        int result
        result = internal
        result = 31 * result + external
        return result
    }

    int getInternal() {
        return internal
    }

    void setInternal(int internal) {
        this.internal = internal
    }

    int getExternal() {
        return external
    }

    void setExternal(int external) {
        this.external = external
    }
}

class DockerContainerHost implements Comparable {
    public String host;
    public String ip;
    int compareTo(Object o) {
        return ComparatorUtils.fieldwiseCompare(this, o);
    }

}

class DockerContainerConstraints implements Comparable {
    public String shm_size;

    int compareTo(Object o) {
        return ComparatorUtils.fieldwiseCompare(this, o);
    }
}

/**
 * Represent a container volume mapping.
 */
class DockerContainerVolume implements Comparable{

    /**
     * Which container to map the volume from. Not neccessary if using a host_path
     */
    public String from_container;

    /**
     * Map from a host path
     */
    public String host_path;

    /**
     * Map from a named volume
     */
    public String volume_name;

    /**
     * Where to map the data to in the container
     */
    public String container_path;

    /**
     * Should the mapping be read-only?
     */
    public boolean read_only = false;

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        DockerContainerVolume that = (DockerContainerVolume) o

        if (read_only != that.read_only) {
            return false
        }
        if (container_path != that.container_path) {
            return false
        }
        if (from_container != that.from_container) {
            return false
        }
        if (host_path != that.host_path) {
            return false
        }
        if (volume_name != that.volume_name) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = (from_container != null ? from_container.hashCode() : 0)
        result = 31 * result + (host_path != null ? host_path.hashCode() : 0)
        result = 31 * result + (volume_name != null ? volume_name.hashCode() : 0)
        result = 31 * result + (container_path != null ? container_path.hashCode() : 0)
        result = 31 * result + (read_only ? 1 : 0)
        return result
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("from_container", from_container)
                .toString();
    }

    int compareTo(Object o) {
        return ComparatorUtils.fieldwiseCompare(this, o);
    }
}


public class DockerContainerInfoState extends ResourceState {
    public JsonNode info;
    public String container_id;

    DockerContainerInfoState(Resource parent,
                             Closure closure) {
        super(parent, closure)
    }



    @JsonIgnore
    Closure getDefaults() {
        Closure defaults = {
            if( provider == null )
                provider = docker_provider(null);
        }
        return defaults;
    }
}

public class DockerContainerState extends ResourceState implements Comparable {

    /**
     * Name for this container.
     */
    public String name;

    /**
     * Image to use for this container.
     */
    public String image;

    /**
     * List of container links, of the format source:target.
     * E.g:
     *
     * ```
     * links = ["postgres:pg_container"]
     * ```
     *
     * Note that you may wish the target to be a container *ID* so that if it changes then this
     * container is also correctly re-created. E.g:
     * ```
     * links = ["postgres:docker_container('pg_container').id"]
     * ```
     */
    public List links;

    /**
     * List of ports to map.
     * E.g:
     * ```
     * ports { internal: 123
     *         external: 123
     *        }
     * ```
     */
    public List<DockerContainerPort> ports = [];

    /**
     * Data volumes / host directory mappings to place into the container
     */
    public List<DockerContainerVolume> volumes = [];

    /**
     * Hosts to add to the container DNS
     */
    public List<DockerContainerHost> host = [];

    /**
     * Array of parameters to pass as the command for the container.
     */
    public List command;
    public List env;

    public Map<String,String> labels = [:];

    /**
     * Restart strategy ('always', 'on-failure', 'unless-stopped')
     */
    public String restart;

    /**
     * Publish all exported ports
     */
    public boolean publish_all_ports;

    public boolean must_run = true;

    /**
     * Net, Host, etc
      */

    public String network_mode;

    /**
     * List of capabilities to expose
     */
    public List<String> capabilities;

    public boolean tty = false;

    public DockerContainerConstraints constraints;

    @NoCompare
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

    public void host(Closure c) {
        DockerContainerHost p = new DockerContainerHost();
        c.delegate = p;
        c.resolveStrategy = Closure.DELEGATE_FIRST

        c()

        host << p;
    }

    public void constraints(Closure c) {
        DockerContainerConstraints p = new DockerContainerConstraints();
        c.delegate = p;
        c.resolveStrategy = Closure.DELEGATE_FIRST

        c()

        constraints = p;
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


    @Override
    public String toString() {
        return """\
DockerContainerState{
    name='$name', 
    image='$image', 
    links=$links, 
    ports=$ports, 
    volumes=$volumes, 
    command=$command, 
    env=$env, 
    restart='$restart', 
    publish_all_ports=$publish_all_ports, 
    must_run=$must_run, 
    id='$id'
}"""
    }
}

/**
 * Represents a docker container.
 */
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

/**
 * Persist into state information about the docker container (obtained by running docker-inspect).
 *
 * Useful so that down-stream consumers may understand the state of the system.
 */
@SGItem("docker_container_info")
public class DockerContainerInfo extends Resource<DockerContainerInfoState> {

    DockerContainerInfo(Module module, String id,
                        Closure closure) {
        super(module, id, closure)
    }

    public PlanAction assess() {
        return new DockerContainerInfoAction(this);
    }


}


public class DockerImageState extends ResourceState {

    public String name;

    public String latest;

    public String imageId;

    public boolean keep_locally;

    /**
     * Which registry provider to use (defaults to the docker hub, or
     * by finding a provider with the same name as the base of the image)
     */
    @JsonIgnore
    public Object registry_provider;

    @JsonIgnore
    String pull_trigger;
    
    DockerImageState(Resource parent, Closure closure) {
        super(parent, closure);
    }

    @JsonIgnore
    Closure getDefaults() {
        Closure defaults = {
            if( provider == null )
                provider = docker_provider(null);

            // If it isn't the global registry, do some sensible
            // default
            if( registry_provider == null ) {
                if( name.contains("/") ) {
                    try {
                        registry_provider = docker_registry(name.substring(0, name.indexOf('/')));
                    } catch(Exception ex) {
                        // this is allowable.
                        // TODO : this throws illegalstateexception. Maybe either be
                        // Optional<> or NotFoundException
                    }
                }

            }
        }
        return defaults;
    }
}

/**
 * Represents a docker image on the local disk.
 */
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

public class PullLogger extends PullImageResultCallback {
    final ThreadLogBase threadLog;

    public PullLogger() {
        threadLog = ThreadLog.get();
    }

    @Override
    void onNext(PullResponseItem item) {
        super.onNext(item)
        threadLog.write(item.toString());
    }
}

/**
 * Represents a docker image present in a remote registry.
 */
@Slf4j
@SGItem("docker_registry_image")
public class DockerRegistryImage extends DataSource<DockerRegistryImageState> {

    DockerRegistryImage(Module module, String id, Closure closure) {
        super(module, id, closure)
    }



    public String getSha256_digest() {
        try {
            return lookupIdInRegistry();
        } catch(Exception ex) {
            // Ignore, revert to pull-to-get-id
        }
        try {
            return pullImageToGetId();
        } catch(Exception ex) {
            //throw new RuntimeException("Failed pulling. Does image actually exist?", ex);
            return null;
        }
    }

    private String lookupIdInRegistry() {
        return DockerRegistryChecker.builder().forImage(state.name).getId();
    }

    private String pullImageToGetId() {
        DockerProvider dp = state.getProvider();
        DockerClient dc = dp.getDockerClient();
        log.info("Pulling docker image ${state.name} for the only purpose of determining the ID. ");

        PullImageCmd cmd = dc.pullImageCmd(state.name);


        if( state.registry != null ) {
            if( !Strings.isNullOrEmpty(state.registry.username) ) {
                AuthConfig ac = new AuthConfig();
                ac.withUsername(state.registry.username);
                ac.withPassword(state.registry.password);

                cmd.withAuthConfig(ac);
            }
        } 


        PullImageResultCallback pullImageResultCallback = cmd.
                exec(new PullLogger());

        try {
            pullImageResultCallback.awaitSuccess();
        } catch(Exception ex) {

            // TODO: If we don't have env vars here (say a destroy or a graph), then
            // we can't actually know which image we want.

            // It could be that we should save the vars (likely). In the iterim, let's just
            // return a stable value as most of the time we don't actually care.
            log.info("Cound not pull image ${state.name}");
            return "unknown-image";
        }

        List<Image> imgs = dc.listImagesCmd().withImageNameFilter(state.name).exec();
        if( imgs.size() != 1 ) {
            // HM. Maybe triton bug?
            imgs = imgs.stream().filter({ img -> img.repoTags.contains(state.name) }).collect();
        }
        
        if( imgs.size() != 1 ) {
            throw new IllegalStateException("Image ${state.name} is ambiguous or empty");
        }

        return imgs.get(0).id;


    }


}

public class DockerRegistryImageState extends DataSourceState {
    public String name;

    public Object registry;

    DockerRegistryImageState(DataSource parent, Closure closure) {
        super(parent, closure)
    }
    Closure getDefaults() {
        Closure defaults = {

            // We need a docker provider in order to be able to
            // talk to the registry
            if( provider == null )
                provider = docker_provider(null);

            // If it isn't the global registry, do some sensible
            // default
            if( registry == null ) {
                if( name.contains("/") ) {
                    try {
                        registry = docker_registry(name.substring(0, name.indexOf('/')));
                    } catch(Exception ex) {
                        // this is allowable.
                        // TODO : this throws illegalstateexception. Maybe either be
                        // Optional<> or NotFoundException
                    }
                }

            }
        }
        return defaults;
    }
//    public String getSha256_digest() {
//        // Do a pull
//    }
}

/**
 * Docker provider : the communications channel with Docker.
 */
@SGItem("docker_provider")
public class DockerProvider extends Provider {
    /**
     * Host. E.g:
     *
     * ```
     * host = "tcp://my.site:2376"
     * ```
     */
    public String host;

    /**
     * If using SSL, where to get certificates from on the local path
     */
    public String cert_path;

    public String key;
    public String ca;
    public String cert;


    DockerProvider(Module module, String id, Closure closure) {
        super(module, id, closure)
    }

    public DockerClient getDockerClient() {


        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host);

        if( cert_path != null ) {
            builder.withDockerTlsVerify(true)
                   .withDockerCertPath(cert_path);

        }

        DefaultDockerClientConfig defaultDockerClientConfig = builder.build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(defaultDockerClientConfig).build();

        return dockerClient;
    }
}

/**
 * Represent a remote docker registry.
 */
@SGItem("docker_registry")
public class DockerRegistry extends Provider {
    /**
     * Username to access the registry with
     */
    public String username;
    /**
     * Password to access the registry with
     */
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

