@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Add calendars triggers
-- ----------------------------
DROP TRIGGER IF EXISTS "trg_calendars_onafter_1" ON "calendar"."calendars";
CREATE TRIGGER "trg_calendars_onafter_1" AFTER INSERT OR UPDATE ON "calendar"."calendars"
FOR EACH ROW
WHEN ((NOT COALESCE((current_setting('webtop.calendar.skip_calendars_history'::text, true))::boolean, false)))
EXECUTE PROCEDURE "calendar"."update_calendars_history"();

DROP TRIGGER IF EXISTS "trg_calendars_onafter_2" ON "calendar"."calendars";
CREATE TRIGGER "trg_calendars_onafter_2" AFTER DELETE ON "calendar"."calendars"
FOR EACH ROW
WHEN ((NOT COALESCE((current_setting('webtop.calendar.skip_calendars_history'::text, true))::boolean, false)))
EXECUTE PROCEDURE "calendar"."update_calendars_history"();

-- ----------------------------
-- Add events triggers
-- ----------------------------
DROP TRIGGER IF EXISTS "trg_events_onafter_1" ON "calendar"."events";
CREATE TRIGGER "trg_events_onafter_1" AFTER INSERT OR DELETE ON "calendar"."events"
FOR EACH ROW
WHEN ((NOT COALESCE((current_setting('webtop.calendar.skip_events_history'::text, true))::boolean, false)))
EXECUTE PROCEDURE "calendar"."update_events_history"();

DROP TRIGGER IF EXISTS "trg_events_onafter_2" ON "calendar"."events";
CREATE TRIGGER "trg_events_onafter_2" AFTER UPDATE OF "revision_timestamp" ON "calendar"."events"
FOR EACH ROW
WHEN ((NOT COALESCE((current_setting('webtop.calendar.skip_events_history'::text, true))::boolean, false)))
EXECUTE PROCEDURE "calendar"."update_events_history"();

-- ----------------------------
-- Cleanup events history
-- ----------------------------
DELETE FROM "calendar"."history_events"
WHERE "calendar_id" IN (SELECT "calendar_id" FROM "calendar"."calendars" WHERE "provider" <> 'local');
