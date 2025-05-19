@DataSource[default@com.sonicle.webtop.calendar]

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
