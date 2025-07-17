package org.deploy.tool.kust.property;

import java.util.List;
import lombok.Data;

@Data
public class EnvProjectInfo {

    private String description;
    private String profile;
    private List<Config> configs;
    private List<Service> services;


}
