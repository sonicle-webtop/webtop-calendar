@DataSource[default@com.sonicle.webtop.calendar]

ALTER TABLE "calendar"."calendars_changes" 
DROP CONSTRAINT IF EXISTS "calendars_changes_calendar_id_fkey",
DROP CONSTRAINT IF EXISTS "calendars_changes_event_id_fkey";
