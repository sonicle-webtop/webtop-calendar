@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Migrate legacy color to the lightest one from default palette (tailwind)
-- ----------------------------
UPDATE "calendar"."calendars" SET "color" = '#F3F4F6' WHERE "color" = '#FFFFFF';
UPDATE "calendar"."calendar_props" SET "color" = '#F3F4F6' WHERE "color" = '#FFFFFF';