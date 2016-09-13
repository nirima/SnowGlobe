package com.nirima.snowglobe.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.exception.NotModifiedException
import com.github.dockerjava.api.model.*
import com.nirima.snowglobe.plan.PlanActionBase

/**
 * Created by magnayn on 05/09/2016.
 */


public class DockerImageAction  extends PlanActionBase<DockerImage,DockerImageState> {

    DockerImageAction(DockerImage pair) {
        super(pair);
    }

    public DockerImageState create(DockerImageState state) {
        return update(state); // Create == Update
    }

    // Functions
    public DockerImageState read(DockerImageState state)
    {

        DockerProvider dp = state.getProvider();
        DockerClient client = dp.getDockerClient();

        List<Image> images = client.listImagesCmd().withImageNameFilter(state.name).exec();
        if( images.size() == 0 )
            state.latest = null;
        state.latest = images.get(0).id;
        return state;
    }


    public DockerImageState update(DockerImageState state) {
//        DockerProvider dp = state.getProvider();
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


}




class DockerContainerAction extends PlanActionBase<DockerContainer,DockerContainerState> {

    DockerContainerAction(DockerContainer resource) {
        super(resource)
    }

    @Override
    DockerContainerState create(DockerContainerState desiredState) {
        DockerProvider dp = desiredState.getProvider();
        DockerClient client = dp.getDockerClient();

        CreateContainerCmd create = client.createContainerCmd(desiredState.image);

        create.withPublishAllPorts(desiredState.publish_all_ports);

        Ports portBindings = new Ports();
        desiredState.ports.each {
            portBindings.bind(ExposedPort.tcp(it.internal), Ports.Binding.bindPort(it.external));

        }
        create.withPortBindings(portBindings);
        if (desiredState.env != null)
            create.withEnv(desiredState.env);

        if (desiredState.command != null)
            create.withCmd(desiredState.command);


        if (desiredState.name != null) {
            create.withName(desiredState.name);
        }

        if (desiredState.links != null) {

            def links = desiredState.links.collect {
                Link.parse(it)
            }

            create.withLinks(links);
        }

        if (desiredState.volumes != null) {

            def volumes = desiredState.volumes.collect {
                VolumesFrom.parse(it.from_container)
            }

            create.withVolumesFrom(volumes)
        }

        CreateContainerResponse response = create.exec();

        if (desiredState.must_run) {
            client.startContainerCmd(response.id).exec();
        }

        desiredState.id = response.id;
        return desiredState;

    }

    @Override
    DockerContainerState delete(DockerContainerState dockerContainer) {
        DockerProvider dp = dockerContainer.getProvider();
        DockerClient client = dp.getDockerClient();

        try {
            client.stopContainerCmd(dockerContainer.id).exec();
        } catch(NotModifiedException ex ){
            // Don't mind actually if it's not running
        }
        client.removeContainerCmd(dockerContainer.id).exec();

        dockerContainer.id = null;
    }


}