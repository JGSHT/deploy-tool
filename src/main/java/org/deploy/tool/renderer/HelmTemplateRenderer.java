package org.deploy.tool.renderer;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HelmTemplateRenderer {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String renderDefaultTemplate(String releaseName, String namespace) {
        Map<String, Object> values = createDefaultValuesStructure(releaseName, namespace);
        return convertToYaml(values);
    }

    private Map<String, Object> createDefaultValuesStructure(String releaseName, String namespace) {
        Map<String, Object> values = new LinkedHashMap<>();

        // 添加文件头注释
        values.put("##", "Auto-generated Helm values.yaml");
        values.put("###", "Generated at: " + LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        values.put("####", "Release: " + releaseName + " | Namespace: " + namespace);

        // 基础配置
        values.put("replicaCount", 1);

        // 镜像配置
        Map<String, Object> image = new LinkedHashMap<>();
        image.put("repository", "registry.example.com/" + releaseName);
        image.put("tag", "latest");
        image.put("pullPolicy", "IfNotPresent");
        values.put("image", image);

        // 服务配置
        Map<String, Object> service = new LinkedHashMap<>();
        service.put("type", "ClusterIP");
        service.put("port", 8080);
        values.put("service", service);

        // 资源限制
        Map<String, Object> resources = new LinkedHashMap<>();
        resources.put("enabled", true);

        Map<String, String> limits = new LinkedHashMap<>();
        limits.put("cpu", "500m");
        limits.put("memory", "512Mi");
        resources.put("limits", limits);

        Map<String, String> requests = new LinkedHashMap<>();
        requests.put("cpu", "200m");
        requests.put("memory", "256Mi");
        resources.put("requests", requests);

        values.put("resources", resources);

        // 自动扩缩容
        Map<String, Object> autoscaling = new LinkedHashMap<>();
        autoscaling.put("enabled", false);
        autoscaling.put("minReplicas", 1);
        autoscaling.put("maxReplicas", 5);
        autoscaling.put("targetCPUUtilizationPercentage", 80);
        values.put("autoscaling", autoscaling);

        // 健康检查
        Map<String, Object> livenessProbe = new LinkedHashMap<>();
        livenessProbe.put("enabled", true);
        livenessProbe.put("path", "/actuator/health");
        livenessProbe.put("initialDelaySeconds", 30);
        livenessProbe.put("periodSeconds", 10);
        values.put("livenessProbe", livenessProbe);

        Map<String, Object> readinessProbe = new LinkedHashMap<>();
        readinessProbe.put("enabled", true);
        readinessProbe.put("path", "/actuator/health/readiness");
        readinessProbe.put("initialDelaySeconds", 15);
        readinessProbe.put("periodSeconds", 5);
        values.put("readinessProbe", readinessProbe);

        // 数据库配置
        Map<String, Object> database = new LinkedHashMap<>();
        database.put("enabled", true);
        database.put("host", "postgres");
        database.put("port", 5432);
        database.put("name", releaseName + "-db");
        database.put("username", releaseName + "-user");
        database.put("password", "CHANGE_ME");
        values.put("database", database);

        // 日志配置
        Map<String, Object> logging = new LinkedHashMap<>();
        logging.put("level", "INFO");
        logging.put("persistence", false);
        logging.put("persistenceSize", "1Gi");
        values.put("logging", logging);

        // 环境变量
        Map<String, Object> env = new LinkedHashMap<>();
        env.put("SPRING_PROFILES_ACTIVE", "k8s");
        env.put("JAVA_OPTS", "-Xmx512m -XX:+UseContainerSupport");
        values.put("env", env);

        // 卷配置
        Map<String, Object> persistence = new LinkedHashMap<>();
        persistence.put("enabled", false);
        persistence.put("size", "5Gi");
        persistence.put("storageClass", "standard");
        values.put("persistence", persistence);

        // Ingress 配置
        Map<String, Object> ingress = new LinkedHashMap<>();
        ingress.put("enabled", false);
        ingress.put("className", "nginx");
        ingress.put("hosts", new String[]{"example.com"});
        values.put("ingress", ingress);

        // 注释配置
        Map<String, Object> annotations = new LinkedHashMap<>();
        annotations.put("prometheus.io/scrape", "true");
        annotations.put("prometheus.io/port", "8080");
        annotations.put("prometheus.io/path", "/actuator/prometheus");
        values.put("podAnnotations", annotations);

        return values;
    }

    private String convertToYaml(Map<String, Object> data) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setIndicatorIndent(2);
        options.setWidth(120);

        Yaml yaml = new Yaml(options);
        return yaml.dump(data);
    }

    public void saveToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
    }


    public String renderTemplateFromFile(String filePath, String releaseName, String namespace) throws IOException {
        String template = Files.readString(Paths.get(filePath));
        return template
                .replace("{{RELEASE_NAME}}", releaseName)
                .replace("{{NAMESPACE}}", namespace)
                .replace("{{TIMESTAMP}}", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    public String renderTemplateFromResource(String resourcePath, String releaseName, String namespace) throws IOException {
        String template = new String(getClass().getResourceAsStream(resourcePath).readAllBytes());
        return template
                .replace("{{RELEASE_NAME}}", releaseName)
                .replace("{{NAMESPACE}}", namespace)
                .replace("{{TIMESTAMP}}", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }
}