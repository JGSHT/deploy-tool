package org.deploy.tool.flyway;

import org.deploy.tool.infra.version.GitPropertyVersion;
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

  @Option(names = {"-h", "--help"}, usageHelp = true, description = "帮助信息", scope = CommandLine.ScopeType.INHERIT)
  private boolean help;

  @Command(name = "publish", description = "数据库基线版本初始化")
  private int publish() {
    return flywayService.publish();
  }

  @Command(name = "migration", description = "数据库更新")
  private int migration(@Option(names = {"-p",
      "--password"}, description = "数据库密码", interactive = true, required = true) String password) {
    return flywayService.migration(password);
  }

  @Command(name = "repair", description = "清除错误执行记录")
  private int repair(@Option(names = {"-p",
      "--password"}, description = "数据库密码", interactive = true, required = true) String password) {
    return flywayService.repair(password);
  }

  @Command(name = "rollback", description = "数据库回滚")
  private int rollback(@Option(names = {"-p", "--password"}, description = "数据库密码", interactive = true, required = true) String password) {
    return flywayService.rollback(password);
  }

}
