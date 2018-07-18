@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Update structure for calendars
-- ----------------------------

ALTER TABLE "calendar"."calendars"
ADD COLUMN "remote_sync_frequency" int2,
ADD COLUMN "remote_sync_timestamp" timestamptz,
ADD COLUMN "remote_sync_tag" varchar(255);

-- ----------------------------
-- Indexes structure for table calendars
-- ----------------------------
CREATE INDEX "calendars_ak3" ON "calendar"."calendars" ("provider", "remote_sync_frequency");
