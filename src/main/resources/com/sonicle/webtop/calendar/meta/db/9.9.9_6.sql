@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Copy data from OLD calendars
-- ----------------------------
INSERT INTO "calendar"."calendars"
SELECT "calendar_id", "domain_id", "user_id", "built_in", "revision_timestamp", "creation_timestamp", "provider", "name", "description", "color", "sync", 
CASE WHEN "is_private" IS true THEN 'PV' ELSE 'PU' END AS def_visibility,
CASE WHEN "busy" IS true THEN 'OP' ELSE 'TP' END AS def_transparency,
"reminder", "notify_on_ext_update", "parameters", "remote_sync_frequency", "remote_sync_timestamp", "remote_sync_tag"
FROM "calendar_old"."calendars";

-- ----------------------------
-- Copy data from OLD calendars_props
-- ----------------------------
INSERT INTO "calendar"."calendars_props"
SELECT *
FROM "calendar_old"."calendar_props"
WHERE "calendar_id" IN (SELECT "calendar_id" FROM "calendar_old"."calendars");

-- ----------------------------
-- Copy data from OLD events
-- ----------------------------
INSERT INTO "calendar"."events"
SELECT
eve0.event_id, 
eve0.calendar_id, 
CASE WHEN rec1.recurrence_id IS NULL THEN rbk1.event_id ELSE NULL END AS series_event_id, 
CASE WHEN rec1.recurrence_id IS NULL THEN replace(rbk1.event_date::VARCHAR, '-', '') ELSE NULL END AS series_instance_id, 
eve0.public_uid, 
eve0.revision_status, 
eve0.revision_timestamp, 
eve0.revision_sequence, 
eve0.creation_timestamp, 
CASE WHEN eve0.read_only IS true THEN 'RO' ELSE 'DF' END AS record_status,
'CF' AS status,
eve0.organizer, 
cal1.user_id AS organizer_id, 
eve0.start_date AS "start", 
eve0.end_date AS "end", 
eve0.timezone, 
eve0.all_day, 
eve0.title, 
eve0."location", 
eve0.description_type, 
eve0.description, 
CASE WHEN eve0.is_private IS true THEN 'PV' ELSE 'PU' END AS visibility,
CASE WHEN eve0.busy IS true THEN 'OP' ELSE 'TP' END AS transparency,
eve0.href, 
eve0.etag, 
eve0.reminder, 
eve0.reminded_at, 
eve0.handle_invitation
FROM "calendar_old"."events" AS eve0
INNER JOIN "calendar_old"."calendars" AS cal1 ON eve0.calendar_id = cal1.calendar_id
LEFT JOIN "calendar_old"."recurrences" AS rec1 ON eve0.recurrence_id = rec1.recurrence_id
LEFT JOIN "calendar_old"."recurrences_broken" AS rbk1 ON eve0.event_id = rbk1.new_event_id
--LEFT JOIN "calendar_old"."recurrences_broken" AS rbk2 ON eve0.recurrence_id = rbk2.recurrence_id
WHERE ((eve0.recurrence_id IS NULL AND rec1.recurrence_id IS NULL) OR (eve0.recurrence_id = rec1.recurrence_id));

-- ----------------------------
-- Copy data from OLD events_attachments
-- ----------------------------
INSERT INTO "calendar"."events_attachments"
SELECT replace("event_attachment_id", '-', ''), "event_id", "revision_timestamp", "revision_sequence", "filename", "size", "media_type"
FROM "calendar_old"."events_attachments"
WHERE "event_id" IN (SELECT "event_id" FROM "calendar"."events");

-- ----------------------------
-- Copy data from OLD events_attachments_data
-- ----------------------------
INSERT INTO "calendar"."events_attachments_data"
SELECT replace("event_attachment_id", '-', ''), "bytes"
FROM "calendar_old"."events_attachments_data"
WHERE replace("event_attachment_id", '-', '') IN (SELECT "event_attachment_id" FROM "calendar"."events_attachments");

-- ----------------------------
-- Copy data from OLD events_attendees
-- ----------------------------
INSERT INTO "calendar"."events_attendees"
SELECT replace("attendee_id", '-', ''), "event_id", "recipient", NULL, "recipient_type", "recipient_role", "response_status", 
CASE WHEN "response_status" NOT IN ('NA') THEN ('1970-01-01 00:00:00+00'::timestamptz) ELSE NULL END AS response_timestamp,
"notify"
FROM "calendar_old"."events_attendees"
WHERE "event_id" IN (SELECT "event_id" FROM "calendar"."events");

-- ----------------------------
-- Copy data from OLD events_custom_values
-- ----------------------------
INSERT INTO "calendar"."events_custom_values"
SELECT *
FROM "calendar_old"."events_custom_values"
WHERE "event_id" IN (SELECT "event_id" FROM "calendar"."events");

-- ----------------------------
-- Copy data from OLD events_icalendars
-- ----------------------------
INSERT INTO "calendar"."events_icalendars"
SELECT *
FROM "calendar_old"."events_icalendars"
WHERE "event_id" IN (SELECT "event_id" FROM "calendar"."events");

-- ----------------------------
-- Copy data from OLD events_recurrences
-- ----------------------------
INSERT INTO "calendar"."events_recurrences"
SELECT "events"."event_id", "recurrences"."start_date", "recurrences"."until_date", "recurrences"."rule"
FROM "calendar_old"."recurrences"
INNER JOIN "calendar_old"."events" ON "recurrences"."recurrence_id" = "events"."recurrence_id"
WHERE "event_id" IN (SELECT "event_id" FROM "calendar"."events");

-- ----------------------------
-- Copy data from OLD events_recurrences_ex
-- ----------------------------
INSERT INTO "calendar"."events_recurrences_ex"
SELECT "event_id", "event_date"
FROM "calendar_old"."recurrences_broken"
WHERE "event_id" IN (SELECT "event_id" FROM "calendar"."events");

-- ----------------------------
-- Copy data from OLD events_tags
-- ----------------------------
INSERT INTO "calendar"."events_tags"
SELECT *
FROM "calendar_old"."events_tags"
WHERE "event_id" IN (SELECT "event_id" FROM "calendar"."events");

-- ----------------------------
-- Copy data from OLD history_calendars
-- ----------------------------
INSERT INTO "calendar"."history_calendars"
SELECT *
FROM "calendar_old"."history_calendars";

-- ----------------------------
-- Copy data from OLD history_events
-- ----------------------------
INSERT INTO "calendar"."history_events"
SELECT *
FROM "calendar_old"."history_events";

-- ----------------------------
-- Add calendars triggers
-- ----------------------------
DROP TRIGGER IF EXISTS "trg_calendars_onafter_1" ON "calendar"."calendars";
CREATE TRIGGER "trg_calendars_onafter_1" AFTER INSERT OR UPDATE ON "calendar"."calendars"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."update_calendars_history"();

DROP TRIGGER IF EXISTS "trg_calendars_onafter_2" ON "calendar"."calendars";
CREATE TRIGGER "trg_calendars_onafter_2" AFTER DELETE ON "calendar"."calendars"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."update_calendars_history"();

-- ----------------------------
-- Add events triggers
-- ----------------------------
DROP TRIGGER IF EXISTS "trg_events_onafter_1" ON "calendar"."events";
CREATE TRIGGER "trg_events_onafter_1" AFTER INSERT OR DELETE ON "calendar"."events"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."update_events_history"();

DROP TRIGGER IF EXISTS "trg_events_onafter_2" ON "calendar"."events";
CREATE TRIGGER "trg_events_onafter_2" AFTER UPDATE OF "revision_timestamp" ON "calendar"."events"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."update_events_history"();

-- ----------------------------
-- Add triggers to keep history tables clean
-- ----------------------------
DROP TRIGGER IF EXISTS "history_calendars_trg_prune" ON "calendar"."history_calendars";
CREATE TRIGGER "history_calendars_trg_prune" AFTER INSERT ON "calendar"."history_calendars"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."prune_calendars_history_on"();

DROP TRIGGER IF EXISTS "history_events_trg_prune" ON "calendar"."history_events";
CREATE TRIGGER "history_events_trg_prune" AFTER INSERT ON "calendar"."history_events"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."prune_events_history_on"();