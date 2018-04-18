# Jenkins Snowglobe per Pull Request


This pattern is useful if you want to create a test environment such that every pull request submitted you your Jenkins Build system is automatically deployed so that it may be subjected to manual or automated testing.

## Steps

### 0: Create docker images in your Jenkins project

You can do this in any way that you like - but in a pipeline you can use the docker-plugin to do this. Here we're pushing to a private registry.

```groovy
    tags = ["my.registry/project:latest", "my.registry/project:${buildnumber}"]
    
    step([$class: 'DockerBuilderPublisher', 
        pushCredentialsId: 'my_registry', 
        cleanImages: false, 
        cleanupWithJenkinsJobDelete: true, 
        dockerFileDirectory: 'foo/bar/docker', 
        pushOnSuccess: true, tagsString: tags]);

```


### 1: Create a 'template' snowglobe project - say 'system-ci'

This configuration can be parameterised - here we use 'name' and 'build' as parameters to pass so that we know which docker image to use (this is the one previously generated in the project)  

This SnowGlobe is never itself applied, it is itself cloned and used per-build.

For example, here is one which uses two images generated in the build (an application and a database container image), and we are using a Joyent Triton cluster to provide docker containers



```groovy

snowglobe
{
  // Clone this snowglobe for a PR build
  // e.g: name  = pr-101
  //      build = 2
    
  
    module("base") {
      
      rt_name = "registry.mycorp.com/project-distribution-${env.name}:${env.build}";
      rt_database = "registry.mycorp.com/project-db-${env.name}:${env.build}";
  
      
        // How to talk to docker itself
        docker_provider {
            host = "tcp://triton-docker.mycorp.com:2376"
            cert_path = "/var/snowglobe/jenkins@triton-cloudapi_mycorp_com"
        }
     
       // Our registry where the images are stored.
       docker_registry("registry.mycorp.com") {
         username = "admin"
         password = "bananafish"
       }
       
      // Define project and the database images as they exist inside the registry
      docker_registry_image("project") {
        name = "${rt_name}"
      }
      docker_registry_image("project_db") {
        name = "${rt_database}"
      }
       
      // Define the image and the database image
      docker_image("project") {
        name = "${docker_registry_image("project").name}"
        pull_trigger = "${docker_registry_image("project").sha256_digest}"
      }
      docker_image("project_db") {
        name = "${docker_registry_image("project_db").name}"
        pull_trigger = "${docker_registry_image("project_db").sha256_digest}"
      }
       
      // Container for the database
      docker_container("project_db") {
          image   = "${docker_image('project_db').latest}"
       
          env = [
            "TZ=Europe/London"
          ]
        }
      
      docker_container_info("project") {
          container_id = "${docker_container('project').id}" 
      }
       
        docker_container("project") {
          image   = "${docker_image('project').latest}"
   
          restart = "always"
          env = [
            "TZ=Europe/London",
            "environmentsource=environment"
          ]
           
          publish_all_ports = true
           
          volumes {           
            from_container = "${docker_container('project_db').id}"               
          }
        }
  
    }
}
``` 
### 3: Spin up the environment in your build

Either by talking REST to the SnowGlobe server, or using the Jenkins snowglobe-plugin, something like:



 
```groovy


 def spinUpTestEnvironment() {
     
       String name1 = "${buildEnv.branch}".replace("/","-").toLowerCase();
 
       String properties = """name="${name1}" 
                              build=${env.BUILD_NUMBER} """;
 
        String name = "${name1}-${env.BUILD_NUMBER}";
 
        // Clone
        snowglobe_clone createAction: true, sourceId: 'ci-template', targetId: name
        
        // Set properties
        snowglobe_set_variables globeId: name, variables: properties
        
        // Apply     
        snowglobe_apply globeId: name, settings: properties
             
        // Response
        def response = snowglobe_state globeId: name
  
        def info_response = readJSON text: response.content;
 
       def ip = info_response['modules']['base']['resources']['docker_container_info']['realtime']['items']['info']['NetworkSettings']['IPAddress'];
 
       return "http://${ip}:8080/";
 }    
 
 ```

This is creating the property values to pass through to the template (so it knows which docker image to spin up).

Here we are also inspect the state of the SnowGlobe (returned as JSON), and pull out a value which is generated in the docker_container_info() node - I.E we return the IP address of the newly spun up system so that the user can be notified (e.g: via slack).

For example :

```groovy
   slackSend channel: "#mycorp_dev", message: "Docker :whale: test environment :snowflake: available now  - ${env.JOB_NAME} ${env.BUILD_NUMBER} - ${testurl}"

```

