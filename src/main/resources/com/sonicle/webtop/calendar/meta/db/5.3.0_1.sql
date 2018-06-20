@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Update structure for calendars
-- ----------------------------

ALTER TABLE "calendar"."calendars"
ADD COLUMN "notify_on_ext_update" bool DEFAULT false NOT NULL;
