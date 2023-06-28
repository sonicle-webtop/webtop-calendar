@DataSource[default@com.sonicle.webtop.calendar]

CREATE OR REPLACE FUNCTION "calendar"."log_calendar_changes"()
  RETURNS TRIGGER AS $BODY$BEGIN
	IF TG_OP = 'INSERT' THEN
		INSERT INTO "calendar"."calendars_changes" ("calendar_id", "event_id", "timestamp", "operation")
		VALUES (NEW."calendar_id", NEW."event_id", NEW."revision_timestamp", 'C');
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
		IF NEW."calendar_id" <> OLD."calendar_id" THEN
			INSERT INTO "calendar"."calendars_changes" ("calendar_id", "event_id", "timestamp", "operation")
			VALUES
			(OLD."calendar_id", NEW."event_id", NEW."revision_timestamp", 'D'),
			(NEW."calendar_id", NEW."event_id", NEW."revision_timestamp", 'C');
		ELSIF NEW."revision_status" <> OLD."revision_status" AND NEW."revision_status" = 'D' THEN
			INSERT INTO "calendar"."calendars_changes" ("calendar_id", "event_id", "timestamp", "operation")
			VALUES (NEW."calendar_id", NEW."event_id", NEW."revision_timestamp", 'D');
		ELSE
			INSERT INTO "calendar"."calendars_changes" ("calendar_id", "event_id", "timestamp", "operation")
			VALUES (NEW."calendar_id", NEW."event_id", NEW."revision_timestamp", 'U');
		END IF;
		RETURN NEW;
	END IF;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;