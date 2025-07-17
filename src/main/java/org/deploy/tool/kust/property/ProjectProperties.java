package org.deploy.tool.kust.property;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("project")
public class ProjectProperties {

    private String description;
    private String defaultProfile;

    private List<EnvProjectInfo> envProjectInfos;

}
