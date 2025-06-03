@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Remove OLD events triggers (also backported to 5.22.0)
-- ----------------------------
DROP TRIGGER IF EXISTS "log_calendar_changes_tr1" ON "calendar"."events";
DROP TRIGGER IF EXISTS "log_calendar_changes_tr2" ON "calendar"."events";
