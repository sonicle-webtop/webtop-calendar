@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Fill history_calendars retrieving data from last-saved status
-- ----------------------------
INSERT INTO "calendar"."history_calendars" ("domain_id", "user_id", "calendar_id", "change_type")
SELECT "calendars"."domain_id", "calendars"."user_id", "calendars"."calendar_id", 'C'
FROM "calendar"."calendars";

-- ----------------------------
-- Fill history_events retrieving data from DEPRECATED table calendars_changes
-- ----------------------------
INSERT INTO "calendar"."history_events" ("calendar_id", "event_id", "change_timestamp", "change_type")
SELECT "calendar_id", "event_id", "timestamp", "operation" FROM "calendar"."calendars_changes" ORDER BY "id";

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
