package org.deploy.tool.kust.config;

import org.deploy.tool.kust.property.ProjectProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(ProjectProperties.class)
@Configuration
public class KustomizeDeployConfig {


}
