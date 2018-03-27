# SnowGlobe
Infrastructure as Code

## What is SnowGlobe

SnowGlobe is a tool in the same area as Terraform, Cloudformation, docker-compose.

SnowGlobe is currently very experimental. It is useful for us in our deployment scenarios!

These tools aim to allow you to 'describe' how infrastructure is to be deployed, and allow incremental changes to be made. Instead of writing scripts that perform steps, your configuration defines what you want the outcome to look like, and the tooling figures out the necessary steps needed to make it work.


It is very similar to (and deeply inspired by) Terraform - but with some differences

* SnowGlobe scripts are code. The script is a DSL script built on top of groovy - so all valid groovy (and thus all valid java) can be used.
  - This should make more 'advanced' deployment scenarios such as blue/green deployments easier to achieve without building external tools that must modify the deployment descriptor.

* SnowGlobe is built on Java/Groovy rather than golang

* Terraform tends to concentrate more on AWS - we are interested (primarily) in Docker, though other providers could easily be added

* It is easier to build 'layers' in SnowGlobe (e.g: a 'base' layer that defines a consul-on-docker, and an 'app' layer that then uses it.

* Terraform is much more mature and has more effort applied to it.

## Docker Image

Running the docker image:

* Use a volume so stored globes & state survive.

```bash

docker run -dtiP -v /Users/store/.snowglobe:/var/snowglobe -v /var/run/docker.sock:/var/run/docker.sock nirima/snowglobe
```

[docs](./docs/index.md) 
