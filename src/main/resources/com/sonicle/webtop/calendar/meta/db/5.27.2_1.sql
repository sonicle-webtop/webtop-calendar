@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Restore history of live events wiped by the old history cleanup task
-- (they were missing from DAV full-syncs)
-- ----------------------------
INSERT INTO "calendar"."history_events" ("calendar_id", "event_id", "change_timestamp", "change_type")
SELECT "events"."calendar_id", "events"."event_id", NOW(), 'C'
FROM "calendar"."events"
WHERE "events"."revision_status" IN ('N','M')
AND NOT EXISTS (
SELECT 1 FROM "calendar"."history_events" AS "his"
WHERE "his"."event_id" = "events"."event_id" AND "his"."calendar_id" = "events"."calendar_id" AND "his"."change_type" IN ('C','U')
);