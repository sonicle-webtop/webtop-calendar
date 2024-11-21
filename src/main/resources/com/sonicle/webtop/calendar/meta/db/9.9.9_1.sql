@DataSource[default@com.sonicle.webtop.calendar]

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
ALTER TABLE "calendar"."calendars_changes" ALTER COLUMN "event_id" TYPE varchar(32) USING "event_id"::varchar(32);

-- ----------------------------
-- Restore Foreign Keys leveraging on event_id
-- ----------------------------
ALTER TABLE "calendar"."events_attachments" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_attendees" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_custom_values" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_icalendars" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_tags" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."recurrences_broken" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."recurrences_broken" ADD FOREIGN KEY ("new_event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;