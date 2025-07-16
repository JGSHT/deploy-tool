package org.deploy.tool.complete;

import org.deploy.tool.version.GitPropertyVersion;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "deploy-tool",
    description = "deploy-tool 是一个针对数据库更新和版本下发的辅助工具",
    versionProvider = GitPropertyVersion.class,
    subcommands = {DBCommand.class, HelmCommand.class}
)
public class RootCommand {

  @Option(names = {"-v", "--version"}, versionHelp = true, description = "版本信息")
  private boolean version;
  @Option(names = {"-h", "--help"}, usageHelp = true, description = "帮助信息")
  private boolean help;

}
