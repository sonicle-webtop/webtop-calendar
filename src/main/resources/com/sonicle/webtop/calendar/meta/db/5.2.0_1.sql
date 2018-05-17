@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Update structure for recurrences
-- ----------------------------
ALTER TABLE "calendar"."recurrences" ALTER COLUMN "type" DROP NOT NULL;

-- ----------------------------
-- Update structure for events
-- ----------------------------
ALTER TABLE "calendar"."events" ADD COLUMN "creation_timestamp" timestamptz;

CREATE INDEX "events_ak4" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "href");

-- ----------------------------
-- Fix data for events
-- ----------------------------
UPDATE "calendar"."events" SET "creation_timestamp" = "revision_timestamp";

UPDATE calendar.events AS cevts
SET
href = cevts.public_uid || '.ics'
FROM calendar.calendars AS ccals
WHERE (cevts.calendar_id = ccals.calendar_id)
AND (cevts.href IS NULL);

-- ----------------------------
-- Update structure for events
-- ----------------------------
ALTER TABLE "calendar"."events"
ALTER COLUMN "creation_timestamp" SET DEFAULT now(), 
ALTER COLUMN "creation_timestamp" SET NOT NULL;

-- ----------------------------
-- Table structure for events_icalendars
-- ----------------------------
CREATE TABLE "calendar"."events_icalendars" (
"event_id" int4 NOT NULL,
"raw_data" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Primary Key structure for table events_icalendars
-- ----------------------------
ALTER TABLE "calendar"."events_icalendars" ADD PRIMARY KEY ("event_id");
