package helm.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
public class DeployProperties {

    private Helm helm;

    @Data
    public static class Helm {
        private String chart;
        private String releaseName;
        private String namespace;
        private String kubeContext;
        private String profile;
        private String outputPath;
        private Boolean showDiff;
        private Boolean atomic;
        private String timeout;
        private Boolean dryRun;

    }
}
