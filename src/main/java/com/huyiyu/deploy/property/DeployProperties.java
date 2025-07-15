package com.huyiyu.deploy.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("deploy")
public class DeployProperties {

    private Flyway flyway;
    private Helm helm;

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
        /**
         * 初始化第一个版本号
         */
        private String baselineVersion = "1.0.0";
        /**
         * 支持第一个版本
         * 初始化更新
         */
        private boolean baselineOnMigrate = true;
        /**
         * 更新文件前缀
         */
        private String migratePrefix = "V";
        /**
         * 更新文件后缀 .sql
         */
        private String migrateSuffix = ".sql";
        /**
         * 更新文件分隔符
         */
        private String migrateSeparator = "_";
        /**
         * 更新历史记录
         * 默认库
         */
        private String schema = "flywaydb";
        /**
         * 默认路径
         */
        private String locations = "filesystem:${user.dir}";
        /**
         * 更新数据库地址
         */
        private String jdbcUrl;
        /**
         * 更新 JDBC 用户
         */
        private String jdbcUser;
        /**
         * 默认仓库地址
         */
        private String repoSQLFile = "flywaydb.zip";
    }

    @Data
    public static class Helm {
        private String chart;
        private String releaseName;
        private String namespace;
        private String kubeContext;
        private String profile;
        private String outputPath;
        private Boolean showDiff;
        private Boolean atomic;
        private String timeout;
        private Boolean dryRun;

    }
}
