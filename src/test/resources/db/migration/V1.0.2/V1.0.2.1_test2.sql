drop table if exists `test`.`test_table2`;
create table `test`.`test_table2`(
                                    `id` bigint primary key auto_increment comment '主键',
                                    `name` varchar(20) comment '名称'
);
insert into `test`.`test_table` values(null,'胡毅宇2');