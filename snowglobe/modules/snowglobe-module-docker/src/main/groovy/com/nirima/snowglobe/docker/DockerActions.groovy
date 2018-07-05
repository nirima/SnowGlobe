package com.nirima.snowglobe.docker

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.InspectContainerResponse
import com.github.dockerjava.api.command.PullImageCmd
import com.github.dockerjava.api.exception.NotModifiedException
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.command.PullImageResultCallback
import com.google.common.base.Strings
import com.nirima.snowglobe.core.Resource
import com.nirima.snowglobe.plan.PlanActionBase
import groovy.util.logging.Slf4j

/**
 * Created by magnayn on 05/09/2016.
 */

@Slf4j
public class DockerImageAction extends PlanActionBase<DockerImage, DockerImageState> {

    DockerImageAction(DockerImage pair) {
        super(pair);
    }

    public DockerImageState create(DockerImageState state) {
        return doUpdate(null, state); // Create == Update
    }

    // Functions
    public DockerImageState read(DockerImageState state) {
        //      return doUpdate(null, state)
    }

    private DockerImageState doUpdate(DockerImageState oldState, DockerImageState state) {

        String name = state.name;

        if (state.pull_trigger != null) {
            if (oldState == null || state.pull_trigger != oldState.latest) {
                // update by ID
                log.info(
                        "Because the pull trigger is different to the current image, we will pull a new docker image from the remote registry.");
                pullImage(state);

            }
        }

        state.latest = findId(state, name);

        if (state.latest == null && !name.contains(":")) {
            state.latest = findId(state, name + ":latest");
        }

        if (state.latest == null) {
            log.error("Could not find image for ${state.name}")
        };

        return state;
    }

    def pullImage(DockerImageState state) {
        state.latest = null;
        DockerProvider dp = state.getProvider();
        DockerClient dc = dp.getDockerClient();

        log.info("Pulling docker image ${state.name}");

        PullImageCmd cmd = dc.pullImageCmd(state.name);

        if (state.registry_provider != null) {
            if (!Strings.isNullOrEmpty(state.registry_provider.username)) {
                AuthConfig ac = new AuthConfig();
                ac.withUsername(state.registry_provider.username);
                ac.withPassword(state.registry_provider.password);

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

    }

    private static String findId(DockerImageState state, String name) {
        DockerProvider dp = state.getProvider();
        DockerClient client = dp.getDockerClient();

        List<Image> images = client.listImagesCmd().withImageNameFilter(name).exec();

        // Triton appears to come back with *multiple* outputs. Ugh!
        Image img = images.find { it.repoTags != null && it.repoTags.contains(name) };

        return img == null ? null : img.id;

    }

    @Override
    public DockerImageState update(DockerImageState oldState, DockerImageState newState) {

        return doUpdate(oldState, newState);
    }
//
//
//
// DockerProvider dp = state.getProvider();
//        DockerClient dc = dp.getDockerClient();
//        PullImageCmd cmd = dc.pullImageCmd(state.name);
//
//        if( state.name.startsWith("registry")) {
//            AuthConfig ac = new AuthConfig();
//            ac.withUsername("admin");
//            ac.withPassword("admin123");
//
//            cmd.withAuthConfig(ac);
//        }
//
//        PullImageResultCallback pullImageResultCallback = cmd.
//                exec(PullImageResultCallback.newInstance());
//
//        pullImageResultCallback.awaitSuccess();
//
//        List<Image> imgs = dc.listImagesCmd().withImageNameFilter(state.name).exec();
//        if( imgs.size() != 1 )
//        {
//            throw new IllegalStateException("Image ${state.name} is ambiguous or empty");
//        }
//
//        state.imageId = imgs.get(0).id;
//        return state;


}

@Slf4j
class DockerContainerInfoAction
        extends PlanActionBase<DockerContainerInfo, DockerContainerInfoState> {

    DockerContainerInfoAction(Resource resource) {
        super(resource)
    }

    @Override
    DockerContainerInfoState read(DockerContainerInfoState desiredState) {
        doIt(desiredState);

    }

    @Override
    DockerContainerInfoState create(DockerContainerInfoState desiredState) {
        doIt(desiredState);
    }

    @Override
    DockerContainerInfoState update(DockerContainerInfoState old,
                                    DockerContainerInfoState newState) {
        doIt(newState);
    }

    DockerContainerInfoState doIt(DockerContainerInfoState desiredState) {
        log.info("Read {}", desiredState);

        DockerProvider dp = desiredState.getProvider();
        DockerClient client = dp.getDockerClient();

        InspectContainerResponse ccr = client.inspectContainerCmd(desiredState.container_id)
                .exec();

        ObjectMapper mapper = new ObjectMapper();
        String sdata = mapper.writeValueAsString(ccr);

        JsonNode rootNode = mapper.readTree(new ByteArrayInputStream(sdata.getBytes()));

        desiredState.info = rootNode;





        return desiredState;
    }
}

@Slf4j
class DockerContainerAction extends PlanActionBase<DockerContainer, DockerContainerState> {

    DockerContainerAction(DockerContainer resource) {
        super(resource)
    }

    @Override
    DockerContainerState read(DockerContainerState desiredState) {
        log.info("Read {}", desiredState);

    }

    @Override
    DockerContainerState create(DockerContainerState desiredState) {

        log.info("Create {}", desiredState);

        DockerProvider dp = desiredState.getProvider();
        DockerClient client = dp.getDockerClient();

        CreateContainerCmd create = client.createContainerCmd(desiredState.image);

        if( desiredState.constraints != null ) {
            HostConfig hostConfig = new HostConfig();
            hostConfig.withShmSize(Long.parseLong(desiredState.constraints.shm_size));
            create.withHostConfig(hostConfig);
        }

        create.withPublishAllPorts(desiredState.publish_all_ports);

        final List<ExposedPort> exposedPortList = new ArrayList<>();

        Ports portBindings = new Ports();
        desiredState.ports.each {
            ExposedPort xp = ExposedPort.tcp(it.internal);
            portBindings.bind(xp, Ports.Binding.bindPort(it.external));
            exposedPortList.add(xp);

        }
        create.withPortBindings(portBindings);
        create.withExposedPorts(exposedPortList);


        if (desiredState.env != null) {
            create.withEnv(desiredState.env)
        };

        if (desiredState.command != null) {
            create.withCmd(desiredState.command)
        };

        if (!Strings.isNullOrEmpty(desiredState.restart)) {
            RestartPolicy restartPolicy = RestartPolicy.parse(desiredState.restart);
            create.withRestartPolicy(restartPolicy);
        }

        if (desiredState.name != null) {
            create.withName(desiredState.name);
        }

        if (desiredState.links != null) {

            def links = desiredState.links.collect {
                Link.parse(it)
            }

            create.withLinks(links);
        }

        if (desiredState.host != null && desiredState.host.size() > 0) {

            List<String> extraHosts = new ArrayList<String>();

            desiredState.host.forEach {
                extraHosts.add(new String("${it.host}:${it.ip}"));

            }
            create.withExtraHosts((List<String>) extraHosts);
        }

        if (desiredState.volumes != null) {

            def volumes = desiredState.volumes.stream().filter
            { it.from_container != null }
                    .collect {
                VolumesFrom.parse(it.from_container)
            }

            create.withVolumesFrom(volumes)

            List<Volume> vols = new ArrayList<>();
            List<Bind> binds = new ArrayList<>();


            desiredState.volumes.forEach
                    {
                        if (it.from_container == null) {
                            Volume v = new Volume(it.container_path);
                            Bind b;

                            if (it.host_path != null) {
                                b = new Bind(it.host_path, v,
                                             AccessMode.fromBoolean(!it.read_only))
                            } else {
                                b = new Bind(it.volume_name, v,
                                             AccessMode.fromBoolean(!it.read_only))
                            };

                            vols.add(v);
                            binds.add(b);
                        }
                    };

            if (vols.size() > 0) {
                create.withVolumes(vols);
                create.withBinds(binds);

            }

        }

        create.withTty(desiredState.tty);

        if (desiredState.network_mode != null) {
            create.withNetworkMode(desiredState.network_mode);
        }

        if (desiredState.capabilities != null) {

            List<Capability> caps = desiredState.capabilities.
                    collect() { Capability.valueOf(Capability.class, it) };

            create.withCapAdd(caps);
        }



        create.withLabels(desiredState.labels);

        CreateContainerResponse response = create.exec();



        if (desiredState.must_run) {
            client.startContainerCmd(response.id).exec();
        }




        desiredState.id = response.id;
        return desiredState;

    }

    @Override
    DockerContainerState update(DockerContainerState old, DockerContainerState newState) {

        if (old.compareTo(newState) != 0) {
            log.info "Docker Container ${newState.name} requires re-creation"
            // Delete and recreate
            delete(old);
            return create(newState);
        }

        // In the case of an update that does nothing, pass through any important
        // values.
        if (newState.id == null) {
            newState.id = old.id
        };

        // Old state will have stuff like IDs in it, so return that if it's deemed
        // to be the same.
        return old;

    }

    @Override
    DockerContainerState delete(DockerContainerState dockerContainer) {

        // No ID == nothing to do.
        if (dockerContainer.id == null) {
            return
        };

        DockerProvider dp = dockerContainer.getProvider();
        DockerClient client = dp.getDockerClient();

        try {
            client.stopContainerCmd(dockerContainer.id).exec();
        } catch (NotModifiedException ex) {
            // Don't mind actually if it's not running
        }
        client.removeContainerCmd(dockerContainer.id).exec();

        dockerContainer.id = null;
        return null;
    }


}