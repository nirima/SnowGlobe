package com.nirima.jenkins.plugins.snowglobe.source;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * Created by magnayn on 10/09/2016.
 */
public abstract class GlobeSourceDescriptor extends Descriptor<GlobeSource>
{
  public static DescriptorExtensionList<GlobeSource,GlobeSourceDescriptor> all() {
    return Jenkins.getInstance().getDescriptorList(GlobeSource.class);
  }
}
