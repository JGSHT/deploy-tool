create database `test` default character set 'utf8mb4';
drop table if exists `test`.`test_table`;
create table `test`.`test_table`
(
    `id`   bigint primary key auto_increment comment '主键',
    `name` varchar(20) comment '名称'
);
insert into `test`.`test_table`
values (null, '胡毅宇');