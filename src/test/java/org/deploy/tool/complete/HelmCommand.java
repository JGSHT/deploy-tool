package org.deploy.tool.complete;

import org.deploy.tool.enums.Operation;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "helm",
        description = "helm 部署管理"
)
@Component
public class HelmCommand {

  @Option(names = {"-v", "--version"}, versionHelp = true, description = "版本信息")
  private boolean version;
  @Option(names = {"-h", "--help"}, usageHelp = true, description = "帮助信息")
  private boolean help;


  @Command(name = "install", description = "Helm 部署")
  private int install() {
    return ExitCode.OK;
  }

  @Command(name = "upgrade", description = "Helm 更新")
  private int upgrade() {
    return ExitCode.OK;
  }

  @Command(name = "uninstall", description = "Helm 卸载")
  private int uninstall() {
    return ExitCode.OK;
  }

  @Command(name = "deploy", description = "Helm 部署")
  private int deploy(
          @Parameters(
          index = "0",
          description = "操作类型: ${COMPLETION-CANDIDATES}",
          arity = "1"
          ) Operation operation) {
    return ExitCode.OK;
  }

  /**
   * 配置文件生成
   * @param outputPath 输出文件路径
   */
    @Command(name = "template", description = "生成template模板")
    public void template(
            @Option(names = {"-o", "--output"}, description = "输出文件路径") String outputPath
    ) {
        ;
    }

  @Command(name = "rollback", description = "Helm 回滚")
  private int rollback(
          @Option(names = {"--revision"}, description = "指定回滚版本号") Integer revision,
          @Option(names = {"--smart"}, description = "智能回滚到上一个稳定版本", defaultValue = "false") boolean smartRollback
  ) {
    return ExitCode.OK;
  }

}
