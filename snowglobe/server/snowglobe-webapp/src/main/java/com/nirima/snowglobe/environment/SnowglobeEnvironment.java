package com.nirima.snowglobe.environment;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.system.EnvironmentVariablesConfigurationSource;

/**
 * Created by magnayn on 26/04/2017.
 */
public class SnowglobeEnvironment {

  private final ConfigurationProvider configurationProvider;

  private SnowglobeEnvironment(ConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
  }

  public static SnowglobeEnvironment build() {

    ConfigurationSource configurationSource = new EnvironmentVariablesConfigurationSource();

    ConfigurationProvider configurationProvider = new ConfigurationProviderBuilder()
        .withConfigurationSource(configurationSource)
        .build();

    return new SnowglobeEnvironment(configurationProvider);
  }

  public boolean isDebug() {

    try {
      return configurationProvider.getProperty("isDebug", Boolean.class);
    } catch (Exception e) {
      return false;
    }
  }

  public String getFileRepositoryRoot() {
    try
    {
      return configurationProvider.getProperty("repositoryRoot", String.class);
    }
    catch (Exception e)
    {
      return null;
    }
  }
}