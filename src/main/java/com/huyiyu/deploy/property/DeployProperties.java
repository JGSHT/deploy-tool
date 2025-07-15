package com.huyiyu.deploy.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("deploy")
public class DeployProperties {

  private Flyway flyway;

  @Data
  public static class Flyway {

    /**
     * 仓库地址
     */
    private String repoUrl;
    /**
     * 用户名
     */
    private String repoUsername;
    /**
     * 密码
     */
    private String repoPassword;
    /**
     * 工作目录
     */
    private String workdir = System.getProperty("user.dir");
    private String baselineVersion = "1.0.0";
    private boolean baselineOnMigrate = true;
    private String migratePrefix = "V";
    private String migrateSuffix = ".sql";
    private String migrateSeparator = "_";
    private String schema = "flywaydb";
    private String locations = "filesystem:${user.dir}";
    private String jdbcUrl;
    private String jdbcUser;

  }
}
