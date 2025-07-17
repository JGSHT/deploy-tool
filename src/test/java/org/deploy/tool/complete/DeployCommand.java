package org.deploy.tool.complete;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;

@Command(name = "deploy", description = "部署相关内容")
@Slf4j
@Component
public class DeployCommand {


  @Command(name = "info", description = "显示当前系统环境信息")
  private int info(
      @Option(names = {"-p", "--profile"}, description = "当前所属环境") String profile) {
    //TODO 打印项目名称,项目有多少个服务实例等信息
    return ExitCode.OK;
  }


  @Command(name = "template", description = "生成模板")
  public int template(
      @Option(names = {"--instances", "-i"}, description = "更新服务实例") List<String> instances) {
    // TODO 渲染所有可部署的内容,
    return ExitCode.OK;
  }

  @Command(name = "update", description = "将服务升级到对应版本,用于日常升级服务")
  public int update(@Option(names = {"-t", "--tag"}) String tag,
      @Option(names = {"--instances", "-i"}, description = "更新服务实例") List<String> instances) {
    //TODO  将服务升级到对应版本,用于日常升级服务
    return ExitCode.OK;
  }

  @Command(name = "backup", description = "备份当前配置信息,并上传到制品库")
  public int backup() {
    //TODO 备份当前配置
    return ExitCode.OK;
  }

  @Command(name = "restore")
  public int restore(@Option(names = {"-v", "--version"}) String version) {
    return ExitCode.OK;
  }

  @Command(name = "restore-by-path")
  public int restoreByPath(@Option(names = {"-p", "--path"}) String path) {
    return ExitCode.OK;
  }


}
