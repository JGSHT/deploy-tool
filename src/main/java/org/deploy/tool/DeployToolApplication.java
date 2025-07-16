package org.deploy.tool;

import org.deploy.tool.flyway.DBCommand;
import org.deploy.tool.helm.HelmCommand;
import org.deploy.tool.infra.version.GitPropertyVersion;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Option;

@SpringBootApplication
@Command(name = "deploy-tool",
        description = "deploy-tool 是一个针对数据库更新和版本下发的辅助工具",
        versionProvider = GitPropertyVersion.class
)
@Component
public class DeployToolApplication implements CommandLineRunner, ExitCodeGenerator {


    @Option(names = {"-v", "--version"}, versionHelp = true, description = "版本信息")
    private boolean version;
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "帮助信息")
    private boolean help;

    /**
     * 退出码
     */
    private int exitcode;
    @Resource
    private IFactory factory;
    @Resource
    private DBCommand dbCommand;
    @Resource
    private HelmCommand helmCommand;


    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(DeployToolApplication.class, args)));
    }


    @Override
    public void run(String... args) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.addSubcommand(dbCommand);
        commandLine.addSubcommand(helmCommand);
        this.exitcode = commandLine.execute(args);
    }

    @Override
    public int getExitCode() {
        return this.exitcode;
    }
}