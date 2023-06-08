@DataSource[default@com.sonicle.webtop.calendar]

INSERT INTO "calendar"."calendars_changes" ("calendar_id", "event_id", "timestamp", "operation")
SELECT "events"."calendar_id", "events"."event_id", "events"."creation_timestamp", 'C'
FROM "calendar"."events"
INNER JOIN "calendar"."calendars" ON "events"."calendar_id" = "calendars"."calendar_id"
WHERE "events"."revision_status" IN ('N','M')
UNION
SELECT "events"."calendar_id", "events"."event_id", "events"."revision_timestamp", 'U'
FROM "calendar"."events"
INNER JOIN "calendar"."calendars" ON "events"."calendar_id" = "calendars"."calendar_id"
WHERE "events"."revision_status" IN ('N','M');

INSERT INTO "calendar"."calendars_changes" ("calendar_id", "event_id", "timestamp", "operation")
SELECT "events"."calendar_id", "events"."event_id", "events"."creation_timestamp", 'D'
FROM "calendar"."events"
INNER JOIN "calendar"."calendars" ON "events"."calendar_id" = "calendars"."calendar_id"
WHERE "events"."revision_status" IN ('D');

DROP TRIGGER IF EXISTS "log_calendar_changes_tr1" ON "calendar"."events";
DROP TRIGGER IF EXISTS "log_calendar_changes_tr2" ON "calendar"."events";

CREATE TRIGGER "log_calendar_changes_tr1" AFTER INSERT OR DELETE ON "calendar"."events"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."log_calendar_changes"();

CREATE TRIGGER "log_calendar_changes_tr2" AFTER UPDATE OF "revision_timestamp" ON "calendar"."events"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."log_calendar_changes"();