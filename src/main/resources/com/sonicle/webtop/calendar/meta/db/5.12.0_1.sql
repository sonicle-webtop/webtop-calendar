@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Deprecate is_default: move values to user_settings
-- ----------------------------
INSERT INTO "core"."user_settings" ("domain_id", "user_id", "service_id", "key", "value")
(
SELECT DISTINCT ON ("calendars"."domain_id", "calendars"."user_id") "calendars"."domain_id", "calendars"."user_id", 'com.sonicle.webtop.calendar', 'calendar.folder.default', "calendars"."calendar_id"
FROM "calendar"."calendars"
WHERE "calendars"."is_default" IS TRUE
)
