@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Update structure for events
-- ----------------------------
ALTER TABLE "calendar"."events" ALTER COLUMN "title" TYPE varchar(255);
