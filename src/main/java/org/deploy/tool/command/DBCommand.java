package org.deploy.tool.command;

import org.deploy.tool.service.FlywayService;
import org.deploy.tool.version.GitPropertyVersion;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "db",
        description = "数据库操作",
        versionProvider = GitPropertyVersion.class
)
@Component
@Slf4j
@RequiredArgsConstructor
public class DBCommand {

    /**
     * 为了保证自动补全,请使用resource 注入
     */
    @Resource
    private FlywayService flywayService;

    @Option(names = {"-h",
            "--help"}, usageHelp = true, description = "帮助信息", scope = CommandLine.ScopeType.INHERIT)
    private boolean help;
    @Option(names = {"-p",
            "--password"}, description = "数据库密码", interactive = true, required = true, scope = CommandLine.ScopeType.INHERIT)
    private String password;

    @Command(name = "baseline", description = "数据库基线版本初始化")
    private int publish() {
        return flywayService.publish();
    }

    @Command(name = "migration", description = "数据库更新")
    private int migration() {
        return flywayService.migration(password);
    }

    @Command(name = "repair", description = "清除错误执行记录")
    private int repair() {
        return flywayService.repair(password);
    }

    @Command(name = "rollback", description = "数据库回滚")
    private int rollback() {
        return flywayService.rollback(password);
    }

}
