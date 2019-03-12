@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Remove obsolete setting keys
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.calendar') AND ("user_settings"."key" = 'calendar.roots.checked');
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.calendar') AND ("user_settings"."key" = 'calendar.folders.checked');
