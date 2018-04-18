# Constituent Parts / Orientation

By default, SnowGlobe stores content on-disk in a text format. In future this will be versioned (via a git repo) so that an audit trail of changes can be followed.

## Parts of each SnowGlobe -- Tabs in the UI

### Source

The source is the SnowGlobe definition file (by default: snowglobe.sg). This file declares "what you want the world to look like" after it is applied.

### Variables

Variables are optional key/value properties that can be read by the configuration. This file is stored in TOML format - e.g:

```
woo="yay"

[user]
name="fred"
```

The properties specified here are 'woo' and 'user.name'.

These properties can be accessed by the script through the 'env' variable. For example

```groovy
docker_container_name = "mycorp/${env.containername}:${env.build}";
```

### Tags

Tags are simple labels attached to a snowglobe. For example, the jenkins plugin uses these to distinguish which SnowGlobes it is responsible for.

### State

The state file stores the snowglobe configuration as it was when the configuration was applied. If an apply fails part-way, this will allow you to resume from where you left off.

The state file is critical in SnowGlobe determining if anything has changed since it last ran. It is in the same format as the source file and is human readable - thus if someone breaks the enviromnment (for example by deleting a container outside of SnowGlobe), the state can be modified to enable 'apply' to be successful once more.

### Docker (Tab)

This is a shortcut utility that if a docker_container_info resource is present in the state file, it displays TCP/IP port mappings.

### Graph (Tab)

This shows a graphviz representation of 
a) Relationships (dependencies) between items
b) Whether it is known that these resources need to be created, updated, deleted or are stable. (It is not neccesarily possible to know the entire list of changes implied by a diff until apply is run).

## Actions

### Validate

Check that the source is syntactically correct.

### Apply

Apply the source SnowGlobe. I.E: create, update and delete resources as required to make the system look like the SnowGlobe Specification, updating the state file at the end of the process.

### Refresh

Reload content

### Destroy

Delete all items in the SnowGlobe configuration. Note that this (almost) the same as applying an empty configuration to a state - or removing all resources from your configuration and applying it.

### Remove

Remove the SnowGlobe from the system.

### Tags

Add/Remove tags to the SnowGlobe.



