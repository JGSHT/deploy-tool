package org.deploy.tool.complete;

import org.deploy.tool.version.GitPropertyVersion;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;

@Command(
    name = "db",
    description = "数据库操作",
    versionProvider = GitPropertyVersion.class
)
public class DBCommand {


  @Option(names = {"-h",
      "--help"}, usageHelp = true, description = "帮助信息", scope = CommandLine.ScopeType.INHERIT)
  private boolean help;
  @Option(names = {"-p",
      "--password"}, description = "数据库密码", interactive = true, required = true, scope = CommandLine.ScopeType.INHERIT)
  private String password;

  @Command(name = "baseline", description = "数据库基线版本初始化")
  private int baseline() {
    return ExitCode.OK;
  }

  @Command(name = "migration", description = "数据库更新")
  private int migration() {
    return ExitCode.OK;
  }

  @Command(name = "repair", description = "清除错误执行记录")
  private int repair() {
    return ExitCode.OK;
  }

  @Command(name = "rollback", description = "数据库回滚")
  private int rollback() {
    return ExitCode.OK;
  }

}
