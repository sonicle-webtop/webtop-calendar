@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Update structure for calendars
-- ----------------------------
ALTER TABLE "calendar"."calendars"
ADD COLUMN "provider" varchar(20),
ADD COLUMN "parameters" text;

-- ----------------------------
-- Fix data
-- ----------------------------
UPDATE "calendar"."calendars" SET "provider" = 'local';

-- ----------------------------
-- Update structure for calendars
-- ----------------------------
ALTER TABLE "calendar"."calendars" ALTER COLUMN "provider" SET NOT NULL;

-- ----------------------------
-- Update structure for events
-- ----------------------------
ALTER TABLE "calendar"."events"
ADD COLUMN "href" varchar(2048),
ADD COLUMN "etag" varchar(2048);
