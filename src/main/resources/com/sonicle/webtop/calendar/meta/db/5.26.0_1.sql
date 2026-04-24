@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Schema: historicize OLD one
-- ----------------------------
ALTER SCHEMA "calendar" RENAME TO "calendar_old";
CREATE SCHEMA "calendar";

-- ----------------------------
-- Table: calendars
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."calendars";
DROP SEQUENCE IF EXISTS "calendar"."seq_calendars";

CREATE SEQUENCE "calendar"."seq_calendars";
CREATE TABLE "calendar"."_tempsetval" AS SELECT setval('"calendar".seq_calendars'::regclass, (SELECT MAX("calendar_id") FROM "calendar_old"."calendars"), TRUE);
DROP TABLE "calendar"."_tempsetval";
CREATE TABLE "calendar"."calendars" (
"calendar_id" int4 NOT NULL DEFAULT nextval('"calendar".seq_calendars'::regclass),
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"built_in" bool NOT NULL DEFAULT false,
"revision_timestamp" timestamptz(6) NOT NULL DEFAULT now(),
"creation_timestamp" timestamptz(6) NOT NULL DEFAULT now(),
"provider" varchar(20) NOT NULL DEFAULT 'local'::character varying,
"name" varchar(50) NOT NULL,
"description" varchar(100),
"color" varchar(20),
"sync" varchar(1) NOT NULL,
"def_visibility" varchar(2) NOT NULL DEFAULT 'PU'::character varying,
"def_transparency" varchar(2) NOT NULL DEFAULT 'TP'::character varying,
"def_reminder" int4,
"notify_on_ext_update" bool NOT NULL DEFAULT false,
"parameters" text,
"remote_sync_frequency" int2,
"remote_sync_timestamp" timestamptz(6),
"remote_sync_tag" varchar(255)
);

ALTER TABLE "calendar"."calendars" ADD PRIMARY KEY ("calendar_id");
CREATE INDEX "calendars_ak1" ON "calendar"."calendars" USING btree ("domain_id", "user_id", "built_in");
CREATE UNIQUE INDEX "calendars_ak2" ON "calendar"."calendars" USING btree ("domain_id", "user_id", "name");
CREATE INDEX "calendars_ak3" ON "calendar"."calendars" USING btree ("provider", "remote_sync_frequency");

-- ----------------------------
-- Table: calendars_props
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."calendars_props";

CREATE TABLE "calendar"."calendars_props" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"calendar_id" int4 NOT NULL,
"hidden" bool,
"color" varchar(20),
"sync" varchar(1)
);

ALTER TABLE "calendar"."calendars_props" ADD PRIMARY KEY ("domain_id", "user_id", "calendar_id");
ALTER TABLE "calendar"."calendars_props" ADD FOREIGN KEY ("calendar_id") REFERENCES "calendar"."calendars" ("calendar_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "calendars_props_ak1" ON "calendar"."calendars_props" USING btree ("calendar_id");

-- ----------------------------
-- Table: events
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events";
CREATE TABLE "calendar"."events" (
"event_id" varchar(32) NOT NULL,
"calendar_id" int4 NOT NULL,
"series_event_id" varchar(32),
"series_instance_id" varchar(8),
"public_uid" varchar(255) NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int4 NOT NULL DEFAULT 0,
"creation_timestamp" timestamptz(6) NOT NULL DEFAULT now(),
"record_status" varchar(2) NOT NULL DEFAULT 'DF'::character varying,
"status" varchar(2) NOT NULL DEFAULT 'CF'::character varying,
"organizer" varchar(650),
"organizer_id" varchar(100),
"start" timestamptz(6) NOT NULL,
"end" timestamptz(6) NOT NULL,
"timezone" varchar(50) NOT NULL,
"all_day" bool NOT NULL,
"title" varchar(255),
"location" varchar(255),
"description_type" varchar(4) NOT NULL DEFAULT 'text'::character varying,
"description" text,
"visibility" varchar(2) NOT NULL DEFAULT 'PU'::character varying,
"transparency" varchar(2) NOT NULL DEFAULT 'TP'::character varying,
"href" varchar(255),
"etag" varchar(255),
"reminder" int4,
"reminded_at" timestamptz(6),
"handle_invitation" bool DEFAULT false
);

ALTER TABLE "calendar"."events" ADD PRIMARY KEY ("event_id");
ALTER TABLE "calendar"."events" ADD FOREIGN KEY ("series_event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE UNIQUE INDEX "events_ak1" ON "calendar"."events" ("series_event_id", "series_instance_id") WHERE "revision_status" NOT IN ('D');
CREATE INDEX "events_ak2" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "start", "end", "reminder", "reminded_at");
CREATE INDEX "events_ak3" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "href");
CREATE INDEX "events_ak4" ON "calendar"."events" USING btree ("revision_status", "start", "reminder", "reminded_at");
CREATE INDEX "events_ak99" ON "calendar"."events" USING btree ("handle_invitation");

-- ----------------------------
-- Table: events_attachments
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events_attachments";
CREATE TABLE "calendar"."events_attachments" (
"event_attachment_id" varchar(32) NOT NULL,
"event_id" varchar(32) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int2 NOT NULL,
"filename" varchar(255) NOT NULL,
"size" int8 NOT NULL,
"media_type" varchar(255) NOT NULL
);

ALTER TABLE "calendar"."events_attachments" ADD PRIMARY KEY ("event_attachment_id");
ALTER TABLE "calendar"."events_attachments" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "events_attachments_ak1" ON "calendar"."events_attachments" ("event_id");

-- ----------------------------
-- Table: events_attachments_data
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events_attachments_data";
CREATE TABLE "calendar"."events_attachments_data" (
"event_attachment_id" varchar(32) NOT NULL,
"bytes" bytea NOT NULL
);

ALTER TABLE "calendar"."events_attachments_data" ADD PRIMARY KEY ("event_attachment_id");
ALTER TABLE "calendar"."events_attachments_data" ADD FOREIGN KEY ("event_attachment_id") REFERENCES "calendar"."events_attachments" ("event_attachment_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table: events_attendees
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events_attendees";
CREATE TABLE "calendar"."events_attendees" (
"attendee_id" varchar(32) NOT NULL,
"event_id" varchar(32) NOT NULL,
"recipient" varchar(650) NOT NULL,
"recipient_user_id" varchar(100),
"recipient_type" varchar(3) NOT NULL,
"recipient_role" varchar(3) NOT NULL,
"response_status" varchar(2) NOT NULL,
"response_timestamp" timestamptz(6),
"notify" bool NOT NULL
);

ALTER TABLE "calendar"."events_attendees" ADD PRIMARY KEY ("attendee_id");
ALTER TABLE "calendar"."events_attendees" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "events_attendees_ak1" ON "calendar"."events_attendees" USING btree ("event_id", "notify");

-- ----------------------------
-- Table: events_custom_values
-- ----------------------------
CREATE TABLE "calendar"."events_custom_values" (
"event_id" varchar(32) NOT NULL,
"custom_field_id" varchar(22) NOT NULL,
"string_value" varchar(255),
"number_value" float8,
"boolean_value" bool,
"date_value" timestamptz(6),
"text_value" text
);

ALTER TABLE "calendar"."events_custom_values" ADD PRIMARY KEY ("event_id", "custom_field_id");
ALTER TABLE "calendar"."events_custom_values" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_custom_values" ADD FOREIGN KEY ("custom_field_id") REFERENCES "core"."custom_fields" ("custom_field_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table: events_icalendars
-- ----------------------------
CREATE TABLE "calendar"."events_icalendars" (
"event_id" varchar(32) NOT NULL,
"raw_data" text
);

ALTER TABLE "calendar"."events_icalendars" ADD PRIMARY KEY ("event_id");
ALTER TABLE "calendar"."events_icalendars" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table: events_recurrences
-- ----------------------------
CREATE TABLE "calendar"."events_recurrences" (
"event_id" varchar(32) NOT NULL,
"start" timestamptz(6) NOT NULL,
"until" timestamptz(6) NOT NULL,
"rule" varchar(255) NOT NULL
);

ALTER TABLE "calendar"."events_recurrences" ADD PRIMARY KEY ("event_id");
ALTER TABLE "calendar"."events_recurrences" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "events_recurrences_ak1" ON "calendar"."events_recurrences" ("start", "until");

-- ----------------------------
-- Table: events_recurrences_ex
-- ----------------------------
CREATE TABLE "calendar"."events_recurrences_ex" (
"event_id" varchar(32) NOT NULL,
"date" date NOT NULL
);

ALTER TABLE "calendar"."events_recurrences_ex" ADD PRIMARY KEY ("event_id", "date");
ALTER TABLE "calendar"."events_recurrences_ex" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table: events_tags
-- ----------------------------
CREATE TABLE "calendar"."events_tags" (
"event_id" varchar(32) NOT NULL,
"tag_id" varchar(22) NOT NULL
);

ALTER TABLE "calendar"."events_tags" ADD PRIMARY KEY ("event_id", "tag_id");
ALTER TABLE "calendar"."events_tags" ADD FOREIGN KEY ("tag_id") REFERENCES "core"."tags" ("tag_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "events_tags_ak1" ON "calendar"."events_tags" USING btree ("tag_id");


-- ----------------------------
-- Table: history_calendars
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."history_calendars";
DROP SEQUENCE IF EXISTS "calendar"."seq_history_calendars";

CREATE SEQUENCE "calendar"."seq_history_calendars";
CREATE TABLE "calendar"."_tempsetval" AS SELECT setval('"calendar".seq_history_calendars'::regclass, (SELECT MAX("id") FROM "calendar_old"."history_calendars"), TRUE);
DROP TABLE "calendar"."_tempsetval";
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
-- Table: history_events
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."history_events";
DROP SEQUENCE IF EXISTS "calendar"."seq_history_events";

CREATE SEQUENCE "calendar"."seq_history_events";
CREATE TABLE "calendar"."_tempsetval" AS SELECT setval('"calendar".seq_history_events'::regclass, (SELECT MAX("id") FROM "calendar_old"."history_events"), TRUE);
DROP TABLE "calendar"."_tempsetval";
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
