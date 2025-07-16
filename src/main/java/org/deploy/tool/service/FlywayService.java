package org.deploy.tool.service;

import org.deploy.tool.dao.FlywayDao;
import org.deploy.tool.http.ArtifactoryExchange;
import org.deploy.tool.property.DeployProperties;
import org.deploy.tool.utils.ZipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.flywaydb.core.Flyway;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import picocli.CommandLine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;

import static org.deploy.tool.constant.DbConstant.VERSION_PATTERN;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlywayService {

    private final DeployProperties deployProperties;
    private final FlywayDao flywayDao;
    private final ArtifactoryExchange artifactoryExchange;


    public int baseline(String password) {
        if (!flywayDao.checkTableExist(password)) {
            log.warn("正在初始化基线版本");
            Flyway flyway = createFlyway(password);
            return flyway.baseline().successfullyBaselined ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
        }
        log.warn("无需初始化基线版本");
        return CommandLine.ExitCode.OK;
    }

    public int publish() {
        if (!checkPathInvalid()) {
            throw new RuntimeException("当前工作目录需要满足fileSystem:db/migration/V*.*.*/*.sql格式");
        }
        File sourceFile = new File(deployProperties.getFlyway().getWorkdir(), "db");
        File targetFile = new File(deployProperties.getFlyway().getWorkdir(), deployProperties.getFlyway().getRepoSQLFile());
        ZipUtil.compress(sourceFile.getAbsolutePath(),targetFile.getAbsolutePath());
        try(FileInputStream inputStream = new FileInputStream(targetFile)){
            artifactoryExchange.push(deployProperties.getFlyway().getRepoSQLFile(), inputStream);
        } catch (IOException e) {
            throw new RuntimeException("上传文件失败",e);
        }
        return CommandLine.ExitCode.OK;
    }

    private boolean checkPathInvalid() {
        File file = new File(deployProperties.getFlyway().getWorkdir(), "db/migration");
        return file.exists() && file.isDirectory();
    }

    public int migration(String password) {
        if (flywayDao.checkTableExist(password)) {
            return baseline(password);
        } else {
            return downZipFileByVersionAndDelete(path -> {
                Flyway flyway = createFlyway(password);
                return flyway.migrate().success ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
            });
        }
    }


    public int repair(String password) {
        if (flywayDao.checkTableExist(password)) {
            Flyway flyway = createFlyway(password);
            flyway.repair();
            return CommandLine.ExitCode.OK;
        } else {
            return baseline(password);
        }
    }

    public int rollback(String password) {
        if (flywayDao.checkTableExist(password)) {
            String rawVersion = flywayDao.getPreviousVersion(password);
            if (!StringUtils.hasText(rawVersion)) {
                throw new RuntimeException("不支持回滚,可能的原因是: 没有可回滚的版本？超过回滚时效");
            }
            Matcher matcher = VERSION_PATTERN.matcher(rawVersion);
            if (!matcher.matches()) {
                throw new RuntimeException("版本号不正确");
            }
            return downZipFileByVersionAndDelete(path -> {
                File parentFile = new File(path, "db/rollback");
                if (parentFile.exists() || parentFile.isDirectory()) {
                    File[] files = parentFile.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            try {
                                String sql = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                                flywayDao.execute(password, sql);
                            } catch (IOException e) {
                                throw new RuntimeException("文件不存在", e);
                            }
                        }
                    }
                }
                return CommandLine.ExitCode.OK;
            });
        } else {
            log.error("请先初始化基线版本");
        }
        return CommandLine.ExitCode.OK;
    }

    private Flyway createFlyway(String password) {
        DeployProperties.Flyway flyway = deployProperties.getFlyway();
        return org.flywaydb.core.Flyway.configure()
                .dataSource(flyway.getJdbcUrl(), flyway.getJdbcUser(), password)
                .baselineVersion(flyway.getBaselineVersion())
                .baselineOnMigrate(flyway.isBaselineOnMigrate())
                .sqlMigrationPrefix(flyway.getMigratePrefix())
                .sqlMigrationSeparator(flyway.getMigrateSeparator())
                .sqlMigrationSuffixes(flyway.getMigrateSuffix())
                .defaultSchema(flyway.getSchema())
                .encoding(StandardCharsets.UTF_8)
                .locations(flyway.getLocations())
                .load();
    }


    private int downZipFileByVersionAndDelete(Function<File, Integer> function) {
        Resource resource = artifactoryExchange.pull(deployProperties.getFlyway().getRepoSQLFile());
        String workdir = deployProperties.getFlyway().getWorkdir();
        File tmp = new File(workdir, UUID.randomUUID().toString());
        try (InputStream inputStream = resource.getInputStream()) {
            File flywayZip = new File(workdir, deployProperties.getFlyway().getRepoSQLFile());
            FileUtils.copyToFile(inputStream, flywayZip);
            ZipUtil.extract(flywayZip, workdir);
            FileUtils.deleteQuietly(flywayZip);
            return function.apply(tmp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(tmp);
        }
    }
}
