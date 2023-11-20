@DataSource[default@com.sonicle.webtop.calendar]

CREATE SCHEMA "calendar";

-- ----------------------------
-- Sequence structure for seq_calendars
-- ----------------------------
DROP SEQUENCE IF EXISTS "calendar"."seq_calendars";
CREATE SEQUENCE "calendar"."seq_calendars";

-- ----------------------------
-- Sequence structure for seq_events
-- ----------------------------
DROP SEQUENCE IF EXISTS "calendar"."seq_events";
CREATE SEQUENCE "calendar"."seq_events";

-- ----------------------------
-- Sequence structure for seq_recurrences
-- ----------------------------
DROP SEQUENCE IF EXISTS "calendar"."seq_recurrences";
CREATE SEQUENCE "calendar"."seq_recurrences";

-- ----------------------------
-- Table structure for calendars
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."calendars";
CREATE TABLE "calendar"."calendars" (
"calendar_id" int4 DEFAULT nextval('"calendar".seq_calendars'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"built_in" bool DEFAULT false NOT NULL,
"provider" varchar(20) DEFAULT 'local'::character varying NOT NULL,
"name" varchar(50) NOT NULL,
"description" varchar(100),
"color" varchar(20),
"sync" varchar(1) NOT NULL,
"is_default" bool DEFAULT false NOT NULL,
"is_private" bool DEFAULT false NOT NULL,
"busy" bool DEFAULT false NOT NULL,
"reminder" int4,
"invitation" bool DEFAULT true NOT NULL,
"notify_on_ext_update" bool DEFAULT false NOT NULL,
"parameters" text,
"remote_sync_frequency" int2,
"remote_sync_timestamp" timestamptz,
"remote_sync_tag" varchar(255)
);

-- ----------------------------
-- Table structure for calendar_props
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."calendar_props";
CREATE TABLE "calendar"."calendar_props" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"calendar_id" int4 NOT NULL,
"hidden" bool,
"color" varchar(20),
"sync" varchar(1)
);

-- ----------------------------
-- Table structure for events
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events";
CREATE TABLE "calendar"."events" (
"event_id" int4 DEFAULT nextval('"calendar".seq_events'::regclass) NOT NULL,
"calendar_id" int4 NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int4 DEFAULT 0 NOT NULL,
"creation_timestamp" timestamptz DEFAULT now() NOT NULL,
"public_uid" varchar(255) NOT NULL,
"recurrence_id" int4,
"read_only" bool NOT NULL,
"start_date" timestamptz(6) NOT NULL,
"end_date" timestamptz(6) NOT NULL,
"timezone" varchar(50) NOT NULL,
"all_day" bool NOT NULL,
"organizer" varchar(650),
"title" varchar(255),
"description" text,
"location" varchar(255),
"is_private" bool NOT NULL,
"busy" bool NOT NULL,
"reminder" int4,
"reminded_on" timestamptz(6),
"href" varchar(2048),
"etag" varchar(2048),
"activity_id" int4,
"master_data_id" varchar(36),
"stat_master_data_id" varchar(36),
"causal_id" int4,
"handle_invitation" bool DEFAULT false
);

-- ----------------------------
-- Table structure for events_attachments
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events_attachments";
CREATE TABLE "calendar"."events_attachments" (
"event_attachment_id" varchar(36) NOT NULL,
"event_id" int4 NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int2 NOT NULL,
"filename" varchar(255) NOT NULL,
"size" int8 NOT NULL,
"media_type" varchar(255) NOT NULL
);

-- ----------------------------
-- Table structure for events_attachments_data
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events_attachments_data";
CREATE TABLE "calendar"."events_attachments_data" (
"event_attachment_id" varchar(36) NOT NULL,
"bytes" bytea NOT NULL
);

-- ----------------------------
-- Table structure for events_attendees
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events_attendees";
CREATE TABLE "calendar"."events_attendees" (
"attendee_id" varchar(36) NOT NULL,
"event_id" int4 NOT NULL,
"recipient" varchar(320) NOT NULL,
"recipient_type" varchar(3) NOT NULL,
"recipient_role" varchar(3) NOT NULL,
"response_status" varchar(2) NOT NULL,
"notify" bool NOT NULL
);

-- ----------------------------
-- Table structure for events_icalendars
-- ----------------------------
CREATE TABLE "calendar"."events_icalendars" (
"event_id" int4 NOT NULL,
"raw_data" text
);

-- ----------------------------
-- Table structure for recurrences
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."recurrences";
CREATE TABLE "calendar"."recurrences" (
"recurrence_id" int4 DEFAULT nextval('"calendar".seq_recurrences'::regclass) NOT NULL,
"start_date" timestamptz(6) NOT NULL,
"until_date" timestamptz(6) NOT NULL,
"repeat" int4,
"permanent" bool,
"type" varchar(1),
"daily_freq" int4,
"weekly_freq" int4,
"weekly_day_1" bool,
"weekly_day_2" bool,
"weekly_day_3" bool,
"weekly_day_4" bool,
"weekly_day_5" bool,
"weekly_day_6" bool,
"weekly_day_7" bool,
"monthly_freq" int4,
"monthly_day" int4,
"yearly_freq" int4,
"yearly_day" int4,
"rule" varchar(255)
);

-- ----------------------------
-- Table structure for recurrences_broken
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."recurrences_broken";
CREATE TABLE "calendar"."recurrences_broken" (
"event_id" int4 NOT NULL,
"recurrence_id" int4 NOT NULL,
"event_date" date NOT NULL,
"new_event_id" int4
);

-- ----------------------------
-- Alter Sequences Owned By 
-- ----------------------------

-- ----------------------------
-- Indexes structure for table calendars
-- ----------------------------
CREATE INDEX "calendars_ak1" ON "calendar"."calendars" USING btree ("domain_id", "user_id", "built_in");
CREATE UNIQUE INDEX "calendars_ak2" ON "calendar"."calendars" USING btree ("domain_id", "user_id", "name");
CREATE INDEX "calendars_ak3" ON "calendar"."calendars" ("provider", "remote_sync_frequency");

-- ----------------------------
-- Primary Key structure for table calendars
-- ----------------------------
ALTER TABLE "calendar"."calendars" ADD PRIMARY KEY ("calendar_id");

-- ----------------------------
-- Indexes structure for table calendar_props
-- ----------------------------
CREATE INDEX "calendar_props_ak1" ON "calendar"."calendar_props" USING btree ("calendar_id");

-- ----------------------------
-- Primary Key structure for table calendar_props
-- ----------------------------
ALTER TABLE "calendar"."calendar_props" ADD PRIMARY KEY ("domain_id", "user_id", "calendar_id");

-- ----------------------------
-- Indexes structure for table events
-- ----------------------------
CREATE INDEX "events_ak1" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "recurrence_id", "start_date", "end_date");
CREATE INDEX "events_ak3" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "recurrence_id", "title");
CREATE INDEX "events_ak2" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "start_date", "end_date", "reminder", "reminded_on");
CREATE INDEX "events_ak4" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "href");
CREATE INDEX "events_ak99" ON "calendar"."events" USING btree ("handle_invitation");

-- ----------------------------
-- Primary Key structure for table events
-- ----------------------------
ALTER TABLE "calendar"."events" ADD PRIMARY KEY ("event_id");

-- ----------------------------
-- Indexes structure for table events_attachments
-- ----------------------------
CREATE INDEX "events_attachments_ak1" ON "calendar"."events_attachments" USING btree ("event_id");

-- ----------------------------
-- Primary Key structure for table events_attachments
-- ----------------------------
ALTER TABLE "calendar"."events_attachments" ADD PRIMARY KEY ("event_attachment_id");

-- ----------------------------
-- Primary Key structure for table events_attachments_data
-- ----------------------------
ALTER TABLE "calendar"."events_attachments_data" ADD PRIMARY KEY ("event_attachment_id");

-- ----------------------------
-- Indexes structure for table events_attendees
-- ----------------------------
CREATE INDEX "events_attendee_ak1" ON "calendar"."events_attendees" USING btree ("event_id", "notify");

-- ----------------------------
-- Primary Key structure for table events_icalendars
-- ----------------------------
ALTER TABLE "calendar"."events_icalendars" ADD PRIMARY KEY ("event_id");

-- ----------------------------
-- Primary Key structure for table events_attendees
-- ----------------------------
ALTER TABLE "calendar"."events_attendees" ADD PRIMARY KEY ("attendee_id");

-- ----------------------------
-- Primary Key structure for table recurrences
-- ----------------------------
ALTER TABLE "calendar"."recurrences" ADD PRIMARY KEY ("recurrence_id");

-- ----------------------------
-- Primary Key structure for table recurrences_broken
-- ----------------------------
ALTER TABLE "calendar"."recurrences_broken" ADD PRIMARY KEY ("event_id", "recurrence_id", "event_date");

-- ----------------------------
-- Foreign Key structure for table "calendar"."events_attachments"
-- ----------------------------
ALTER TABLE "calendar"."events_attachments" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Key structure for table "calendar"."events_attachments_data"
-- ----------------------------
ALTER TABLE "calendar"."events_attachments_data" ADD FOREIGN KEY ("event_attachment_id") REFERENCES "calendar"."events_attachments" ("event_attachment_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Key structure for table "calendar"."events_attendees"
-- ----------------------------
ALTER TABLE "calendar"."events_attendees" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Key structure for table "calendar"."events_icalendars"
-- ----------------------------
ALTER TABLE "calendar"."events_icalendars" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Align service version
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
DELETE FROM "core"."settings" WHERE ("settings"."service_id" = 'com.sonicle.webtop.calendar') AND ("settings"."key" = 'manifest.version');
INSERT INTO "core"."settings" ("service_id", "key", "value") VALUES ('com.sonicle.webtop.calendar', 'manifest.version', '5.4.0');
