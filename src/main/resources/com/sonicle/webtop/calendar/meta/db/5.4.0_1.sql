@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Add integrity constraints
-- ----------------------------
DELETE FROM "calendar"."events_attendees" WHERE "event_id" NOT IN (SELECT "events"."event_id" FROM "calendar"."events");
ALTER TABLE "calendar"."events_attendees" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
DELETE FROM "calendar"."events_icalendars" WHERE "event_id" NOT IN (SELECT "events"."event_id" FROM "calendar"."events");
ALTER TABLE "calendar"."events_icalendars" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- New table: events_attachments
-- ----------------------------
CREATE TABLE "calendar"."events_attachments" (
"event_attachment_id" varchar(36) NOT NULL,
"event_id" int4 NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int2 NOT NULL,
"filename" varchar(255) NOT NULL,
"size" int8 NOT NULL,
"media_type" varchar(255) NOT NULL
);

CREATE INDEX "events_attachments_ak1" ON "calendar"."events_attachments" USING btree ("event_id");
ALTER TABLE "calendar"."events_attachments" ADD PRIMARY KEY ("event_attachment_id");
ALTER TABLE "calendar"."events_attachments" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- New table: events_attachments_data
-- ----------------------------
CREATE TABLE "calendar"."events_attachments_data" (
"event_attachment_id" varchar(36) NOT NULL,
"bytes" bytea NOT NULL
);

ALTER TABLE "calendar"."events_attachments_data" ADD PRIMARY KEY ("event_attachment_id");
ALTER TABLE "calendar"."events_attachments_data" ADD FOREIGN KEY ("event_attachment_id") REFERENCES "calendar"."events_attachments" ("event_attachment_id") ON DELETE CASCADE ON UPDATE CASCADE;
