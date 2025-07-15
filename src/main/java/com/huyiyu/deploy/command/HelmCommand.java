package com.huyiyu.deploy.command;

import com.huyiyu.deploy.enums.Operation;
import com.huyiyu.deploy.service.DeployService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "helm",
        description = "helm 部署管理"
)
@Component
@Slf4j
@RequiredArgsConstructor
public class HelmCommand {

    @Option(names = {"-v", "--version"}, versionHelp = true, description = "版本信息")
    private boolean version;
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "帮助信息")
    private boolean help;

    private final DeployService deployService;

    @Command(name = "install", description = "Helm 部署")
    private int install() {
        return deployService.install();
    }

    @Command(name = "upgrade", description = "Helm 更新")
    private int upgrade() {
        return deployService.upgrade();
    }

    @Command(name = "uninstall", description = "Helm 卸载")
    private int uninstall() {
        return deployService.uninstall();
    }

    @Command(name = "deploy", description = "Helm 部署")
    private int deploy(
            @Parameters(
                    index = "0",
                    description = "操作类型: ${COMPLETION-CANDIDATES}",
                    arity = "1"
            ) Operation operation) {
        return deployService.deploy(operation);
    }

    /**
     * 配置文件生成
     *
     * @param outputPath 输出文件路径
     */
    @Command(name = "template", description = "生成template模板")
    public void template(
            @Option(names = {"-o", "--output"}, description = "输出文件路径") String outputPath
    ) {
        deployService.template(outputPath);
    }

    @Command(name = "rollback", description = "Helm 回滚")
    private int rollback(
            @Option(names = {"--revision"}, description = "指定回滚版本号") Integer revision,
            @Option(names = {"--smart"}, description = "智能回滚到上一个稳定版本", defaultValue = "false") boolean smartRollback
    ) {
        return deployService.rollback(revision, smartRollback);
    }

}
