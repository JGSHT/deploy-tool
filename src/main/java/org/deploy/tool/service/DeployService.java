package org.deploy.tool.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.deploy.tool.diff.DiffGenerator;
import org.deploy.tool.enums.Operation;
import org.deploy.tool.property.DeployProperties;
import org.deploy.tool.renderer.HelmTemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeployService {

    private final DiffGenerator diffGenerator;
    private final HelmTemplateRenderer templateRenderer;
    private final DeployProperties deployProperties;

    /**
     * helm install my-app ./src/main/resources/chart \
     * -f ./values/dev-values.yaml \
     * --namespace dev \
     * --create-namespace \
     * --debug
     *
     * @return 部署结果
     */
    public int install() {
        System.out.println("开始helm部署");
        return deploy(Operation.install);
    }

    public int upgrade() {
        System.out.println("开始执行helm更新");
        return deploy(Operation.upgrade);
    }

    public int uninstall() {
        System.out.println("开始执行helm卸载");
        return deploy(Operation.uninstall);
    }


    public int deploy(Operation operation) {
        System.out.println("chartName=" + deployProperties.getHelm().getChart());
        System.out.println("releaseName=" + deployProperties.getHelm().getReleaseName());
        System.out.println("namespace=" + deployProperties.getHelm().getNamespace());
        try {

            if (deployProperties.getHelm().getShowDiff() && (operation == Operation.upgrade || operation == Operation.install)) {
                System.out.println("正在生成 diff");
                showDiff(operation);
            }

            List<String> command = buildHelmCommand(operation);
            executeCommand(command);

            if (operation == Operation.upgrade || operation == Operation.install) {
                checkHealth();
            }

            return 0;
        } catch (Exception e) {
            System.err.println("Helm 操作失败: " + e.getMessage());
            return 1;
        }
    }

    private void showDiff(Operation operation) throws Exception {
        String currentManifest = getCurrentManifest(operation);
        String newManifest = getNewManifest();

        String diff = diffGenerator.generateDiff(currentManifest, newManifest,
                "当前版本", "新版本 (" + operation + ")");

        System.out.println("版本差异对比:");
        System.out.println(diff);

        // 等待用户确认
        System.out.print("是否继续部署? (y/n): ");
        Scanner scanner = new Scanner(System.in);
        if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.println("部署已取消");
            System.exit(0);
        }
    }

    private String getCurrentManifest(Operation operation) throws Exception {
        if (operation == org.deploy.tool.enums.Operation.upgrade) {
            return executeAndCapture(List.of(
                    "helm", "get", "manifest", deployProperties.getHelm().getReleaseName(), "--namespace", deployProperties.getHelm().getNamespace()
            ));
        }
        return "";
    }

    private String getNewManifest() throws Exception {
        List<String> templateCmd = new ArrayList<>();
        templateCmd.add("helm");
        templateCmd.add("template");
        templateCmd.add(deployProperties.getHelm().getReleaseName());
        templateCmd.add(resolveChartPath());
        templateCmd.add("--namespace=" + deployProperties.getHelm().getNamespace());

        return executeAndCapture(templateCmd);
    }

    private List<String> buildHelmCommand(Operation operation) {
        List<String> cmd = new ArrayList<>();
        cmd.add("helm");

        // 添加通用参数
        addGlobalOptions(cmd);

        // 添加操作特定参数
        switch (operation) {
            case install:
                cmd.add("install");
                cmd.add(deployProperties.getHelm().getReleaseName());
                cmd.add(resolveChartPath());
                addUpgradeOptions(operation, cmd);
                cmd.add("--description=Deployed by CLI at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                break;

            case upgrade:
                cmd.add("upgrade");
                cmd.add(deployProperties.getHelm().getReleaseName());
                cmd.add(resolveChartPath());
                addUpgradeOptions(operation, cmd);
                cmd.add("--description=Upgraded by CLI at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                break;

            case uninstall:
                cmd.add("uninstall");
                cmd.add(deployProperties.getHelm().getReleaseName());
                cmd.add("--namespace=" + deployProperties.getHelm().getNamespace());
                cmd.add("--description=Uninstalled by CLI at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                break;

            case status:
                cmd.add("status");
                cmd.add(deployProperties.getHelm().getReleaseName());
                cmd.add("--namespace=" + deployProperties.getHelm().getNamespace());
                break;

            case history:
                cmd.add("history");
                cmd.add(deployProperties.getHelm().getReleaseName());
                cmd.add("--namespace=" + deployProperties.getHelm().getNamespace());
                cmd.add("--max=10");
                break;

            case test:
                cmd.add("test");
                cmd.add(deployProperties.getHelm().getReleaseName());
                cmd.add("--namespace=" + deployProperties.getHelm().getNamespace());
                cmd.add("--logs");
                break;

            case lint:
                cmd.add("lint");
                cmd.add(resolveChartPath());
                break;
        }

        return cmd;
    }

    private void addGlobalOptions(List<String> cmd) {
        cmd.add("--namespace=" + deployProperties.getHelm().getNamespace());
        if (deployProperties.getHelm().getKubeContext() != null)
            cmd.add("--kube-context=" + deployProperties.getHelm().getKubeContext());
        if (deployProperties.getHelm().getAtomic()) cmd.add("--atomic");
        cmd.add("--timeout=" + deployProperties.getHelm().getTimeout());
    }

    private void addUpgradeOptions(Operation operation, List<String> cmd) {
        // 添加动态值
        Map<String, String> dynamicValues = generateDynamicValues(operation);
        dynamicValues.forEach((k, v) -> {
            cmd.add("--set");
            cmd.add(k + "=" + v);
        });
    }

    private Map<String, String> generateDynamicValues(Operation operation) {
        Map<String, String> values = new HashMap<>();
        values.put("metadata.deployer", System.getProperty("user.name"));
        values.put("metadata.timestamp", Instant.now().toString());

        if (operation == Operation.install) {
            values.put("metadata.initialDeploy", "true");
        }

        return values;
    }

    private String resolveChartPath() {
        Path localPath = Paths.get(deployProperties.getHelm().getChart());
        if (localPath.toFile().exists()) {
            return localPath.toAbsolutePath().toString();
        }
        return deployProperties.getHelm().getChart();
    }

    private void executeCommand(List<String> command) throws IOException, InterruptedException {
        System.out.println("🚀 执行 Helm 命令: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Helm 命令执行失败，退出码: " + exitCode);
        }
    }

    private void checkHealth() throws Exception {
        System.out.println("🩺 检查应用健康状态...");

        List<String> cmd = List.of(
                "kubectl", "rollout", "status", "deployment/" + deployProperties.getHelm().getReleaseName(),
                "--namespace=" + deployProperties.getHelm().getNamespace(),
                "--watch=true",
                "--timeout=5m"
        );

        executeCommand(cmd);
        System.out.println("应用状态健康");
    }

    private String executeAndCapture(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("命令执行失败，退出码: " + exitCode);
        }

        return output.toString();
    }

    /**
     * helm template my-app ./jr --namespace my-namespace > my-app.yaml
     *
     * @param outputPath 模板文件输出路径
     */
    public void template(String outputPath) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add("helm");
            cmd.add("template");
            cmd.add(deployProperties.getHelm().getReleaseName());
            cmd.add(resolveChartPath());
            cmd.add("--namespace=" + deployProperties.getHelm().getNamespace());
            if (outputPath == null) {
                outputPath = deployProperties.getHelm().getOutputPath();
            }
            String outputDir = deployProperties.getFlyway().getWorkdir() + outputPath;
            cmd.add("--output-dir=" + outputDir);
            executeCommand(cmd);
            System.out.println("Yaml 文件生成到: " + outputDir);
        } catch (Exception e) {
            System.err.println("模板生成失败: " + e.getMessage());
        }
    }

    public int rollback(Integer revision, boolean smartRollback) {
        try {
            if (smartRollback) {
                return performSmartRollback();
            } else if (revision != null) {
                return performRollback(revision);
            } else {
                System.err.println("必须指定 --revision 或使用 --smart");
                return 1;
            }
        } catch (Exception e) {
            System.err.println("🚨  回滚失败: " + e.getMessage());
            return 1;
        }
    }

    private Integer performSmartRollback() throws Exception {
        System.out.println("🧠 执行智能回滚...");

        // 获取发布历史
        String historyJson = executeAndCapture(List.of(
                "helm", "history", deployProperties.getHelm().getReleaseName(), "--namespace", deployProperties.getHelm().getNamespace(), "-o", "json"
        ));

        // 解析历史记录
        JsonArray history = JsonParser.parseString(historyJson).getAsJsonArray();
        int lastSuccessful = -1;
        int currentRevision = -1;

        for (JsonElement el : history) {
            JsonObject item = el.getAsJsonObject();
            String status = item.get("status").getAsString();
            int rev = item.get("revision").getAsInt();

            if (status.equals("deployed")) {
                lastSuccessful = rev;
            }

            if (status.equals("deployed") || status.equals("failed")) {
                currentRevision = rev;
            }
        }

        if (lastSuccessful > 0 && lastSuccessful != currentRevision) {
            System.out.println("🔙 回滚到稳定版本: " + lastSuccessful);
            return performRollback(lastSuccessful);
        } else {
            System.out.println("ℹ️ 未找到可回滚的稳定版本");
            return 0;
        }
    }

    private Integer performRollback(int revision) throws Exception {
        System.out.println("↩️ 回滚到版本: " + revision);

        List<String> cmd = List.of(
                "helm", "rollback", deployProperties.getHelm().getReleaseName(), String.valueOf(revision),
                "--namespace", deployProperties.getHelm().getNamespace(),
                "--description=Rollback initiated by CLI"
        );

        executeCommand(cmd);
        System.out.println("✅ 回滚成功");
        return 0;
    }

}
