@DataSource[default@com.sonicle.webtop.calendar]

DROP TRIGGER IF EXISTS "log_calendar_changes_tr1" ON "calendar"."events";

CREATE TRIGGER "log_calendar_changes_tr1" AFTER INSERT ON "calendar"."events"
FOR EACH ROW
EXECUTE PROCEDURE "calendar"."log_calendar_changes"();

