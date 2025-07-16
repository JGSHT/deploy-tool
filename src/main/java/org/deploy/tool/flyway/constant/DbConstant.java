package org.deploy.tool.flyway.constant;

import java.util.regex.Pattern;

public interface DbConstant {

  String FILE_SYSTEM_PREFIX = "filesystem:";
  Pattern VERSION_PATTERN = Pattern.compile("(?<version>\\d+\\.\\d+\\.\\d+)\\.\\d+");

  /**
   * 查询初始化表是否存在
   */
  String CHECK_EXIST_SQL = """
      SELECT 
        COUNT(*) as count
      FROM
        information_schema.`tables` t
      WHERE
       t.TABLE_SCHEMA='flywaydb' 
      AND
       t.TABLE_NAME = 'flyway_schema_history'
      """;
  /**
   * 查询上一次更新在回滚周期内的版本
   */
  String GET_PREVIOUS_VERSION_SQL = """
       SELECT 
         version  
       FROM 
         flywaydb.flyway_schema_history
       WHERE installed_on BETWEEN ? AND ?
       ORDER BY 
         installed_rank DESC
       LIMIT 1
      """;

  String GET_ROLLBACK_HISTORY_ID_BY_VERSION_SQL = """
       SELECT 
         installed_rank  
       FROM 
         flywaydb.flyway_schema_history
       WHERE version like ?
      """;

  String DELETE_BY_INSTALLED_RANK = """
      DELETE FROM 
      flywaydb.flyway_schema_history
      WHERE 
      installed_rank =?
      """;

}
