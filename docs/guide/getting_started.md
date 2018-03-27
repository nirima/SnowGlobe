# Getting Started

## Raw Commandline

If you are familiar with Docker, you will be used to spinning up containers in a docker system by using the command line. Here, various parameters can be sent as flags to control the settings for the container.

For example, the following command can be used to instantiate a snowglobe container
```bash
docker run -dtiP -v /Users/store/.snowglobe:/var/snowglobe -v /var/run/docker.sock:/var/run/docker.sock nirima/snowglobe
```

Here, we have various options - run in the background, map all ports, plus map a couple of locations in the 'outer' system into the containe filesystem space.

## Compose

As these settings become harder to remember, and commandlines get longer, you may start to use things like docker-compose in order to manage containers and the dependencies between them. For example, a docker-compose script for the equivalent may look something like this::

```yaml

version: '3'
services:
  snowglobe:
    image: "nirima/snowglobe"    
    ports:
     - "8088:8088"
    volumes:
     - /Users/store/.snowglobe:/var/snowglobe
     - /var/run/docker.sock:/var/run/docker.sock    
```

This is nice, as it is both easier to understand and modify, and it will attempt to understand the dependencies between containers in order to spin them up in the correct order.

However, the docker-compose here is limited only to docker containers - often we want to perform additional steps to non-docker resources (e.g: loading a key/value store, spinning up an AWS host). Also, it you are shipping a system which has different configuration options (e.g: is this a 'demo' system against a test db, a 'production' system against a 'proper' database, is this option on or off, are there extra containers for caching to be started depending on the scenario). Also it isn't easy to make this work for scenarios like 'red/green' deployment where we wish to only tear down the old container once the new, upgraded one has been started and is servicing clients.

## Configuration as code

SnowGlobe was heavily inspired by Terraform, which follows a pattern of "configuration as code". SnowGlobe offers different tradeoffs to Terraform by being based on a Groovy DSL (domain specific language) - thus you have the ability to extend the system with the full Groovy and Java ecosystem, and it offers additional 'metaprogramming' constructs to help simplify the way complex deployments can be described.

### Concepts

In "Configuration as Code", we describe the way in which we wish our deployment environment to look in one or more configuration files. These files may be versioned and stored in a repository. When we want to make the changes live, we tell the SnowGlobe system to 'apply' the changes. It then looks at the previous state of the system, anything it can determine about the running system, and figures out what changes need to be applied.

In the case of Docker, if you changed (for example) a port mapping, applying that change would force the re-creation of the container with the new settings.


### Diving In...


The following configuration is the equivalent SnowGlobe script to spin up a Docker version of SnowGlobe

```groovy
snowglobe
{
    module("base") {
     
        docker_provider {
            host = "unix:///var/run/docker.sock"
        }
      
        docker_container("snowglobe") {
          image   = "nirima/snowglobe"
          name    = "snowglobe"
		  
          publish_all_ports = true

          volumes {            
    			host_path = "/Users/magnayn/.snowglobe"  
				container_path = "/var/snowglobe"
          }
         
          volumes {            
    			host_path = "/var/run/docker.sock"
				container_path = "/var/run/docker.sock"
          }
          
        }    
    }
}
```
If you store this script in a new SnowGlobe configuration (if using the web instance, click 'new' and give it a name) - by default "snowglobe.sg", then hit 'apply' - provided everything is running correctly, it will spin up your new container. 


This ought to be relatively easy to understand. The main unit in a SnowGlobe script is a module. SnowGlobe will automatically calculate dependencies between resources and modules, and evaluate modules in order. Thus if you have (for example) a container that needs a separate container with pre-loaded values - you can be sure that it will be up and running before the secondary module is considered.

Here we have one *provider* and one *resource*. The docker provider is how we talk to docker, and the resource itself uses that provider to talk to it.

Modules, resources and providers are all *named* - if you do not specify a name, it is given the name of 'null', which is the *default*. 

Note that the docker_container here has an implicit default:

```groovy
          docker_container("snowglobe") {
                   image   = "nirima/snowglobe"
                   name    = "snowglobe"
                   // Don't need to write this, as it's the default
                   //provider = docker_provider(null)
            
                   // ...
            }            
```

#### State

After apply is completed, you will notice a 'state' file is created. Looking inside it you will see all of the final values that were used - and would be used in determining whether a resource needed to be re-created.

The format is the same DSL format as the main SnowGlobe script - and can be modified in unusual circumstances.

Here you can see items that are not in the initial script - such as the ID chosen for the container by docker. This helps SnowGlobe understand the mapping between configured resources and actual containers present in docker.

In a deployment scenario, you would want this state file to be persisted, as it is important in the process of determining what has changed. 

```groovy
snowglobe {
   module("base") { 
     docker_container("snowglobe") {
          name = "snowglobe"
          image = "nirima/snowglobe"
          links = null
          volumes { 
          from_container = null
          host_path = "/Users/magnayn/.snowglobe"
          volume_name = null
          container_path = "/var/snowglobe"
          read_only = false
          }
          volumes { 
          from_container = null
          host_path = "/var/run/docker.sock"
          volume_name = null
          container_path = "/var/run/docker.sock"
          read_only = false
          }
          command = null
          env = null
          labels = [:]
          restart = null
          publish_all_ports = true
          must_run = true
          network_mode = null
          capabilities = null
          tty = false
          id = "faa2b9b04a553ca5af7ab75c6cf4d77b89383d1f814ab18d9ca5db8e46338058"
          resource = null
          provider =  docker_provider(null)
     }
   }
}

```

### Functions

SnowGlobe has only a small number of functions

- Apply 
  Take the configuration specified, determine how to make the real system look like it, and perform functions to reconcile the two.
  
- Destroy
  Tear down any applied resources (this is semantically identical to applying an empty configuration)

In addition there are features to examine which changes will be done if 'apply' is run, and to generate graphs to show this.
  
  
## A more complex example : Docker Registries

The previous configuration omits a number of technicalities that become important if you want advanced functionality. For example

- You may want your container to re-create if a new version of the image is available locally

- You may want to pull a new image from a registry if a new version is available

Let's create a script that does that:

```groovy

snowglobe
{
  
    module("base") {
            
        // How to talk to docker itself
        docker_provider {
            host = "tcp://triton-docker.foobar.com:2376"
            cert_path = "/var/snowglobe/triton-cloudapi_foobar_com"
        }
     
       // Our registry where the images are stored.
       docker_registry("registry.foobar.com") {
         username = "admin"
         password = "admin123"
       }
       
      // Define product and the database images as they exist inside the registry
      docker_registry_image("product") {
        name = "registry.foobar.com/product:latest"
      }
            
      // Define the image and the database image
      docker_image("product") {
        name = "${docker_registry_image("product").name}"
        pull_trigger = "${docker_registry_image("product").sha256_digest}"
      }
             
        docker_container("product") {
          image   = "${docker_image('product').latest}"
          restart = "always"
          env = [
            "TZ=Europe/London"       
          ]
           
          publish_all_ports = true
          
        }
  
    }
}

```

Here we declare an additional provider : the private docker registry images are stored in.

We create an additional resource and a data provider 

- An image, representing the docker image on the host

- A 'registry image', representing the state of the image in the repository.

When 'apply' is run, SnowGlobe calculates the dependencies of docker_container -> docker_image -> docker_registry_image. It does this by looking at the variable expansion :- these are simply standard groovy string expansions.

When the registry image is evaluated, it fetches the SHA256 digest for the image asked for in the name.

We then look at docker_image, where that sha256_digest is filled in to the pull_trigger. If this value is *different* from any previously recorded sha256, then it will then choose to pull this new image and update its' state.

Finally the docker container is considered. If the value of docker_image's 'latest' (which, again, is in turn the SHA256 from the image), it will be considered to be different, and hence the container re-created.

## Programmability

SnowGlobe scripts are *groovy* scripts, so we can leverage this to enable re-use in a number of ways.

Firstly, all valid groovy can be used. Want 3 identical containers?

```groovy
snowglobe {
  module("base") {
        for (i = 0; i <3; i++) {
         	docker_container("mycontainer${i}") {
         	  // ....
         	}
    	}
  }
}
```  

### Functions

Functions can be defined to simplify repetitive configuration. For example - the stanza for "docker registry image, docker image, docker container" could be captured into a function, and called from within the script:

```groovy
def docker_stanza(sg, img, _name, fn) {
      
        sg.docker_container(_name) {
          image   = "${docker_image(_name).latest}"
          name    = _name
          
		  fn(delegate);
        }

        sg.docker_image(_name) {
            name = "${docker_registry_image(_name).name}"
            pull_trigger = "${docker_registry_image(_name).sha256_digest}"
        }
        
        sg.docker_registry_image(_name) {
            name = img
        }    
}



snowglobe{
  

  
  module("base") {
      docker_provider {
            host = "unix:///var/run/docker.sock"
        }            
    docker_stanza(delegate,"jenkins:latest", "jenkins2", 
        { 
            x ->  
        
            x.restart = "always"
        
            x.env = [
            "TZ=Europe/London", "foo=bar"           
            ]
        });
    
  }
}

```

### Meta-programming

In the scenario where there are various different deployment options, you may wish to have a 'shared library', which you then have individual SnowGlobe deployments defined in a much simpler way.

For example:

```groovy

package com.nirima

snowglobe
{
           def product = load '../deploy-common/system.sg';

           product { env ->
              env.name='demo';
              env.database='postgres';
           }
}

```

This entire configuration is taken from a separate script, in a relative directory. Note that this is making several decisions about exactly which containers to have in the configuration depending on what parameters are passed in the top-level config.

```groovy
// Commons
import groovy.transform.Field

@Field
String name = 'default';

// Settings that the configuration can use to 
// set up the system

class Settings
{
	def name;
	def database = 'h2'; // default
    def customer;
	boolean consul = true;
  
}

// Function to define a base module
def baseModule(env) {
	module("base") {
        docker_provider {
            host = "unix:///var/run/docker.sock"
        }

        docker_registry("registry.mycorp.com") {
            username = "admin"
            password = "foobar"
        }
   }
}

// Function to define a product module
def productModule(rtsettings) {

	module("rt") {
                def rt_branch = "dev-main"; // Usually 'dev-main'
                def rt_repo   = "product"; // Usually 'product'
                def rt_customer_db = "";

  

                imports {
                    using module('base').docker_provider(null);
                }

                consul_provider {
                    address = "localhost:8500"
                    datacenter = "DC1"
                    scheme = "http"
                    token = ""
                }

                docker_container("product") {

                    image = "${docker_image("product").name}"
                    name = "${rtsettings.name}_product"
                   
                    restart = "always"


                    env = ["consul=consul:8500",
                            "CONSUL=consul", 
                            "CONSUL_AGENT=1",
                            "TZ=Europe/London"]

                    publish_all_ports = true

                    ports {
                        internal = 8888
                        external = 8080
                    }

                    ports {
                        internal = 8000
                        external = 8000
                    }

                    ports {
                        internal = 18888
                        external = 18888
                    }

                    ports {
                        internal = 18898
                        external = 18898
                    }
                    

                    if( rtsettings.database == 'postgres' ) {
                    	links = ["postgres:${module('database').docker_container('postgres').name}",
                    			 "consul:${module('consul').docker_container('consul').name}"]
                    } else {
                        // IH
                    	links = ["${module('consul').docker_container('consul').name}:consul"]
                    }


                    if( rtsettings.database == 'h2' ) {
	                    volumes {
	                        from_container = "${module('database').docker_container('product_db').id}"
	                    }
	                }
                }

               
                docker_image("product") {
                    name = "${docker_registry_image("product").name}"
                    pull_trigger = "${docker_registry_image("product").sha256_digest}"
                }

               
                docker_registry_image("product") {
                    name = "registry.mycorp.com/${rt_repo}-distribution-${rt_branch}:latest"
                }

                consul_key_prefix("myapp_config") {
                	println ("myapp: ${rtsettings}");

                    // Prefix to add to prepend to all of the subkey names below.
                    path_prefix = "product/config/"

                    subkeys = [
                            "mpr/db/type"                         : "${rtsettings.database}",

							"mpr/db/username"                         : "product",
							"mpr/db/password"                         : "product",

							"mpr/db/name"							 : "product",
							"mpr/db/host"							 : "postgres",

							"mpr/db/adminusername"                         : "postgres",
							"mpr/db/adminpassword"                         : "abrakebabra",


                    ]
                }

            }
}

def ihModule(settings) {
    module("integration") {
         
            imports {
                using module('base').docker_provider(null);
            }

            consul_provider {
                    address = "localhost:8500"
                    datacenter = "DC1"
                    scheme = "http"
                    token = ""
            }

            docker_container("ih") {

                    image = "${docker_image("ih").name}"
                    name = "${settings.name}_ih"
                   
                    restart = "always"

                    env = ["consul=consul:8500",
                            "CONSUL=consul", 
                            "CONSUL_AGENT=1",
                            "consulEnvironment=integration-hub",
                            "TZ=Europe/London"]

                    publish_all_ports = true

                    ports {
                        internal = 8088
                        external = 8088
                      }

                      ports {
                        internal = 8844
                        external = 8844
                      }

                    ports {
                        internal = 8845
                        external = 8845
                      }

                    ports {
                        internal = 8846
                        external = 8846
                      }

                    ports {
                        internal = 8855
                        external = 8855
                      }

                    ports {
                        internal = 8001
                        external = 8001
                      }

                    ports {
                        internal = 18889
                        external = 18889
                      }

                    links = ["${module('consul').docker_container('consul').name}:consul"]

                    

                }



                    consul_key_prefix("ih_config") {
                    
                    // Prefix to add to prepend to all of the subkey names below.
                    path_prefix = "integration-hub/config/"


                    subkeys = [
  
                             "mpr/db/type"                         : "h2",

                    ]
                }

               


                docker_image("ih") {
                    name = "${docker_registry_image("ih").name}"
                    pull_trigger = "${docker_registry_image("ih").sha256_digest}"
                }

               
                docker_registry_image("ih") {
                    name = "registry.mycorp.com/ih-distribution-dev-main:latest"
                }


            }
}

def h2Module(env) {
	module("database") {

        imports {
                    using module('base').docker_provider(null);
                }


		 docker_container("product_db") {

            image = "${docker_image("product_db").name}"
            name = "${env.name}_product_db"
            must_run = false
        }

           docker_image("product_db") {
                name = "${docker_registry_image("product_db").name}"
                pull_trigger = "${docker_registry_image("product_db").sha256_digest}"
            }
        
          docker_registry_image("product_db") {

                if(env.customer == null ) {                        
                     name = "registry.mycorp.com/product-db-dev-main:latest"
                }
                else
                {                
                    name = "registry.mycorp.com/product-customer-${env.customer}-db-dev-main:latest"
                }
            

               
          }
    }

}

// Define a postgres database container
def postgresModule(settings) {
	module("database") {
		
        imports {
            using module('base').docker_provider(null);
        }

        docker_container("postgres") {
            image = "${docker_image("postgres").name}"
            name = "${settings.name}_postgres"
            restart = "always"

            ports {
                internal = 5432
                external = 15432
            }

            env = [
            	"TZ=Europe/London",
            	"POSTGRES_PASSWORD=abrakebabra"
            	]
        }

        docker_image("postgres") {
			name = "${docker_registry_image("postgres").name}"
   			pull_triggers = "${docker_registry_image("postgres").sha256_digest}"
        }

        docker_registry_image("postgres") {
            name = "postgres:9.6.1"        
        }

	}
}

// Define a consul module where config is contained.
def consulModule(settings) {
	module("consul") {
        imports {
            using module('base').docker_provider(null);
        }

        docker_container("consul") {
            image = "${docker_image("consul").name}"
            name = "${settings.name}_consul"
            restart = "always"

            ports {
                internal = 8300
                external = 8300
            }

            ports {
                internal = 8301
                external = 8301
            }

            ports {
                internal = 8302
                external = 8302
            }

            ports {
                internal = 8500
                external = 8500
            }

            command = ["agent", "-dev", "-client", "0.0.0.0", "-bind", "0.0.0.0"]

            env = ["TZ=Europe/London"]
        }

        docker_image("consul")
                {
                    name = "${docker_registry_image("consul").name}"
                    pull_trigger = "${docker_registry_image("consul").sha256_digest}"
                }

        docker_registry_image("consul") {
            name = "consul:0.7.5"
        }

    }
}

// This is the call defined
def call(Closure c) {

	Object env = new Settings();

	def x = c(env);

	// Define a base module to talk to docker
 	baseModule(env)

 	consulModule(env)

    productModule(env)

    if( env.database == 'postgres' )
    	postgresModule(env);

    if( env.database == 'h2' )
        h2Module(env);

    if( env.include_integration_hub ) {
        ihModule(env);
    }

}

```