@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Support new MasterData table
-- ----------------------------

ALTER TABLE "calendar"."events"
ALTER COLUMN "customer_id" TYPE varchar(36),
ALTER COLUMN "statistic_id" TYPE varchar(36);
ALTER TABLE "calendar"."events" RENAME "customer_id" TO "master_data_id";
ALTER TABLE "calendar"."events" RENAME "statistic_id" TO "stat_master_data_id";
