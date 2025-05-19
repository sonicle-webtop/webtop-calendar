@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Add field description_type
-- ----------------------------
ALTER TABLE "calendar"."events" ADD COLUMN "description_type" varchar(4) NOT NULL DEFAULT 'text';

-- ----------------------------
-- Drop Foreign Keys leveraging on event_id
-- ----------------------------
ALTER TABLE "calendar"."events_icalendars" DROP CONSTRAINT IF EXISTS "events_icalendars_event_id_fkey";
ALTER TABLE "calendar"."events_custom_values" DROP CONSTRAINT IF EXISTS "events_custom_values_event_id_fkey";
ALTER TABLE "calendar"."events_attendees" DROP CONSTRAINT IF EXISTS "events_attendees_event_id_fkey";
ALTER TABLE "calendar"."events_attachments" DROP CONSTRAINT IF EXISTS "events_attachments_event_id_fkey";

-- ----------------------------
-- Update event_id field type
-- ----------------------------
ALTER TABLE "calendar"."events" ALTER COLUMN "event_id" TYPE varchar(32) USING "event_id"::varchar(32);
ALTER TABLE "calendar"."events_attachments" ALTER COLUMN "event_id" TYPE varchar(32) USING "event_id"::varchar(32);
ALTER TABLE "calendar"."events_attendees" ALTER COLUMN "event_id" TYPE varchar(32) USING "event_id"::varchar(32);
ALTER TABLE "calendar"."events_custom_values" ALTER COLUMN "event_id" TYPE varchar(32) USING "event_id"::varchar(32);
ALTER TABLE "calendar"."events_icalendars" ALTER COLUMN "event_id" TYPE varchar(32) USING "event_id"::varchar(32);
ALTER TABLE "calendar"."events_tags" ALTER COLUMN "event_id" TYPE varchar(32) USING "event_id"::varchar(32);
ALTER TABLE "calendar"."recurrences_broken" ALTER COLUMN "event_id" TYPE varchar(32) USING "event_id"::varchar(32);
ALTER TABLE "calendar"."recurrences_broken" ALTER COLUMN "new_event_id" TYPE varchar(32) USING "new_event_id"::varchar(32);

-- ----------------------------
-- Restore Foreign Keys leveraging on event_id
-- ----------------------------
ALTER TABLE "calendar"."events_attachments" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_attendees" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_custom_values" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_icalendars" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
DELETE FROM "calendar"."events_tags" WHERE "event_id" NOT IN (SELECT ev."event_id" FROM "calendar"."events" ev);
ALTER TABLE "calendar"."events_tags" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
DELETE FROM "calendar"."recurrences_broken" WHERE "event_id" NOT IN (SELECT ev."event_id" FROM "calendar"."events" ev);
ALTER TABLE "calendar"."recurrences_broken" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
DELETE FROM "calendar"."recurrences_broken" WHERE "new_event_id" NOT IN (SELECT ev."event_id" FROM "calendar"."events" ev);
ALTER TABLE "calendar"."recurrences_broken" ADD FOREIGN KEY ("new_event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Add revision_timestamp and creation_timestamp fields
-- ----------------------------
ALTER TABLE "calendar"."calendars" 
ADD COLUMN "revision_timestamp" timestamptz,
ADD COLUMN "creation_timestamp" timestamptz;

UPDATE "calendar"."calendars" SET
"revision_timestamp" = '1970-01-01 00:00:00+00'::timestamptz,
"creation_timestamp" = '1970-01-01 00:00:00+00'::timestamptz
WHERE "revision_timestamp" IS NULL;

ALTER TABLE "calendar"."calendars" 
ALTER COLUMN "revision_timestamp" SET NOT NULL,
ALTER COLUMN "revision_timestamp" SET DEFAULT now(),
ALTER COLUMN "creation_timestamp" SET NOT NULL,
ALTER COLUMN "creation_timestamp" SET DEFAULT now();

-- ----------------------------
-- New table: history_calendars
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."history_calendars";
DROP SEQUENCE IF EXISTS "calendar"."seq_history_calendars";

CREATE SEQUENCE "calendar"."seq_history_calendars";
CREATE TABLE "calendar"."history_calendars" (
"id" int8 NOT NULL DEFAULT nextval('"calendar".seq_history_calendars'::regclass),
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"calendar_id" int4 NOT NULL,
"change_timestamp" timestamptz NOT NULL DEFAULT '1970-01-01 00:00:00+00'::timestamptz,
"change_type" char(1) NOT NULL
);

ALTER TABLE "calendar"."history_calendars" ADD PRIMARY KEY ("id");
CREATE INDEX "history_calendars_ak1" ON "calendar"."history_calendars" USING btree ("calendar_id", "change_timestamp");
CREATE INDEX "history_calendars_ak2" ON "calendar"."history_calendars" USING btree ("domain_id", "user_id", "calendar_id", "change_timestamp");

-- ----------------------------
-- New table: history_events
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."history_events";
DROP SEQUENCE IF EXISTS "calendar"."seq_history_events";

CREATE SEQUENCE "calendar"."seq_history_events";
CREATE TABLE "calendar"."history_events" (
"id" int8 NOT NULL DEFAULT nextval('"calendar".seq_history_events'::regclass),
"calendar_id" int4 NOT NULL,
"event_id" varchar(32) NOT NULL,
"change_timestamp" timestamptz NOT NULL DEFAULT '1970-01-01 00:00:00+00'::timestamptz,
"change_type" char(1) NOT NULL
);

ALTER TABLE "calendar"."history_events" ADD PRIMARY KEY ("id");
CREATE INDEX "history_events_ak1" ON "calendar"."history_events" USING btree ("calendar_id", "change_timestamp");
CREATE INDEX "history_events_ak2" ON "calendar"."history_events" USING btree ("event_id", "change_timestamp");
