package org.deploy.tool.flyway.dao;

import static org.deploy.tool.flyway.constant.DbConstant.CHECK_EXIST_SQL;
import static org.deploy.tool.flyway.constant.DbConstant.DELETE_BY_INSTALLED_RANK;
import static org.deploy.tool.flyway.constant.DbConstant.GET_PREVIOUS_VERSION_SQL;
import static org.deploy.tool.flyway.constant.DbConstant.GET_ROLLBACK_HISTORY_ID_BY_VERSION_SQL;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class FlywayDao extends AbstractJdbcTemplateDaoSupport {




  public boolean checkTableExist(String password) {
    return smartExecuteSql(password,
        jdbcTemplate -> jdbcTemplate.queryForLong(CHECK_EXIST_SQL) > 0L);
  }

  public String getPreviousVersion(String password) {

    return smartExecuteSql(password, jdbcTemplate -> {
      String nowStr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
          .format(LocalDateTime.now());
      String yesterdayStr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
          .format(LocalDateTime.now().plusDays(-1L));
      return jdbcTemplate.queryForString(GET_PREVIOUS_VERSION_SQL, yesterdayStr, nowStr);
    });
  }

  public void deleteHistoryByVersion(String password, String rawVersion) {
    smartExecuteSql(password, jdbcTemplate -> {
      List<String> ids = jdbcTemplate.query(GET_ROLLBACK_HISTORY_ID_BY_VERSION_SQL,
          result -> result.getString("installed_rank"), rawVersion + '%');
      for (String id : ids) {
        jdbcTemplate.execute(DELETE_BY_INSTALLED_RANK,id);
      }
      return null;
    });
  }
}
