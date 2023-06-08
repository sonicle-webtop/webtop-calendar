@DataSource[default@com.sonicle.webtop.calendar]

DROP TABLE IF EXISTS "calendar"."calendars_changes";
DROP SEQUENCE IF EXISTS "calendar"."seq_calendars_changes";

CREATE SEQUENCE "calendar"."seq_calendars_changes";
CREATE TABLE "calendar"."calendars_changes" (
  "id" int8 NOT NULL DEFAULT nextval('"calendar".seq_calendars_changes'::regclass),
  "calendar_id" int4 NOT NULL,
  "event_id" int4 NOT NULL,
  "timestamp" timestamptz NOT NULL,
  "operation" char(1) NOT NULL
);

ALTER TABLE "calendar"."calendars_changes" ADD PRIMARY KEY ("id");
CREATE INDEX "calendars_changes_ak1" ON "calendar"."calendars_changes" USING btree ("calendar_id", "timestamp");
CREATE INDEX "calendars_changes_ak2" ON "calendar"."calendars_changes" USING btree ("event_id", "timestamp");
ALTER TABLE "calendar"."calendars_changes" ADD FOREIGN KEY ("calendar_id") REFERENCES "calendar"."calendars" ("calendar_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."calendars_changes" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;