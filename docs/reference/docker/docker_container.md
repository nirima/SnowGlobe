---------------------
# docker_container
 Represents a docker container.
 
- name (String)

 Name for this container.
     

- image (String)

 Image to use for this container.
     

- links (List)

 List of container links, of the format source:target.
 E.g:

 ```
 links = ["postgres:pg_container"]
 ```

 Note that you may wish the target to be a container *ID* so that if it changes then this
 container is also correctly re-created. E.g:
 ```
 links = ["postgres:docker_container('pg_container').id"]
 ```
     

- ports (List)

 List of ports to map.
 E.g:
 ```
 ports { internal: 123
         external: 123
        }
 ```
     

  ## DockerContainerPort
   Created by magnayn on 04/09/2016.
 
  - internal (int)

     Port inside the container
     

  - external (int)

     Port to map to externally
     

- volumes (List)

 Data volumes / host directory mappings to place into the container
     

  ## DockerContainerVolume
   Represent a container volume mapping.
 
  - from_container (String)

     Which container to map the volume from. Not neccessary if using a host_path
     

  - host_path (String)

     Map from a host path
     

  - volume_name (String)

     Map from a named volume
     

  - container_path (String)

     Where to map the data to in the container
     

  - read_only (boolean)

     Should the mapping be read-only?
     

- host (List)

 Hosts to add to the container DNS
     

  ## DockerContainerHost
  
  - host (String)

    

  - ip (String)

    

- command (List)

 Array of parameters to pass as the command for the container.
     

- env (List)



- labels (Map)



- restart (String)

 Restart strategy ('always', 'on-failure', 'unless-stopped')
     

- publish_all_ports (boolean)

 Publish all exported ports
     

- must_run (boolean)



- network_mode (String)

 Net, Host, etc
      

- capabilities (List)

 List of capabilities to expose
     

- tty (boolean)



- id (String)



- provider (Object)



