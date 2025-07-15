package com.huyiyu.deploy.service;

import com.huyiyu.deploy.dao.FlywayDao;
import com.huyiyu.deploy.http.ArtifactoryExchange;
import com.huyiyu.deploy.property.DeployProperties;
import com.huyiyu.deploy.utils.ZipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;

import static com.huyiyu.deploy.constant.DbConstant.FILE_SYSTEM_PREFIX;
import static com.huyiyu.deploy.constant.DbConstant.VERSION_PATTERN;

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
                            String sql = FileUtils.readAsString(Paths.get(file.getAbsolutePath()));
                            flywayDao.execute(password, sql);
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
        String templatFileName = UUID.randomUUID().toString() + ".zip";
        File file = new File(deployProperties.getFlyway().getWorkdir(), templatFileName);
        Resource resource = artifactoryExchange.pull(deployProperties.getFlyway().getRepoSQLFile());
        try (InputStream inputStream = resource.getInputStream()) {
            ZipUtil.unpack(inputStream, file);
            return function.apply(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (file.exists() && file.isDirectory()) {
                FileSystemUtils.deleteRecursively(file);
            }
        }
    }
}
