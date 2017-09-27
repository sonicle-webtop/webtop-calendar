@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Fix structure for calendars
-- ----------------------------
ALTER TABLE "calendar"."calendars"
ALTER COLUMN "provider" SET DEFAULT 'local'::character varying, ALTER COLUMN "provider" SET NOT NULL;
