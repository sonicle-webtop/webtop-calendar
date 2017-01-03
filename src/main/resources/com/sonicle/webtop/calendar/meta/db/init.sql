
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
"built_in" bool DEFAULT false,
"name" varchar(50) NOT NULL,
"description" varchar(100),
"color" varchar(20),
"sync" varchar(1) NOT NULL,
"busy" bool DEFAULT false NOT NULL,
"reminder" int4,
"invitation" bool DEFAULT true NOT NULL,
"is_private" bool DEFAULT false NOT NULL,
"is_default" bool DEFAULT false NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for events
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events";
CREATE TABLE "calendar"."events" (
"event_id" int4 DEFAULT nextval('"calendar".seq_events'::regclass) NOT NULL,
"calendar_id" int4 NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"public_uid" varchar(255) NOT NULL,
"recurrence_id" int4,
"read_only" bool NOT NULL,
"start_date" timestamptz(6) NOT NULL,
"end_date" timestamptz(6) NOT NULL,
"timezone" varchar(50) NOT NULL,
"all_day" bool NOT NULL,
"organizer" varchar(650),
"title" varchar(100),
"description" text,
"location" varchar(255),
"is_private" bool NOT NULL,
"busy" bool NOT NULL,
"reminder" int4,
"reminded_on" timestamptz(6),
"activity_id" int4,
"customer_id" varchar(100),
"statistic_id" varchar(100),
"causal_id" int4
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for events_attendees
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."events_attendees";
CREATE TABLE "calendar"."events_attendees" (
"attendee_id" varchar(36) NOT NULL,
"event_id" int4,
"recipient" varchar(320),
"recipient_type" varchar(20),
"response_status" varchar(20),
"notify" bool
)
WITH (OIDS=FALSE)

;

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
"type" varchar(1) NOT NULL,
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
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for recurrences_broken
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."recurrences_broken";
CREATE TABLE "calendar"."recurrences_broken" (
"event_id" int4 NOT NULL,
"recurrence_id" int4 NOT NULL,
"event_date" date NOT NULL,
"new_event_id" int4
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Alter Sequences Owned By 
-- ----------------------------

-- ----------------------------
-- Indexes structure for table calendars
-- ----------------------------
CREATE INDEX "calendars_ak1" ON "calendar"."calendars" USING btree ("domain_id", "user_id", "built_in");
CREATE UNIQUE INDEX "calendars_ak2" ON "calendar"."calendars" USING btree ("domain_id", "user_id", "name");

-- ----------------------------
-- Primary Key structure for table calendars
-- ----------------------------
ALTER TABLE "calendar"."calendars" ADD PRIMARY KEY ("calendar_id");

-- ----------------------------
-- Indexes structure for table events
-- ----------------------------
CREATE INDEX "events_ak1" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "recurrence_id", "start_date", "end_date");
CREATE INDEX "events_ak3" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "recurrence_id", "title");
CREATE INDEX "events_ak2" ON "calendar"."events" USING btree ("calendar_id", "revision_status", "start_date", "end_date", "reminder", "reminded_on");

-- ----------------------------
-- Primary Key structure for table events
-- ----------------------------
ALTER TABLE "calendar"."events" ADD PRIMARY KEY ("event_id");

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
