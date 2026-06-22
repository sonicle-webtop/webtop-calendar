@DataSource[default@com.sonicle.webtop.calendar]

CREATE OR REPLACE FUNCTION "calendar"."update_events_history"()
  RETURNS TRIGGER AS $BODY$BEGIN
	IF TG_OP = 'DELETE' THEN
		INSERT INTO "calendar"."history_events" ("calendar_id", "event_id", "change_timestamp", "change_type")
		VALUES (OLD."calendar_id", OLD."event_id", NOW(), 'D');
		RETURN OLD;
	ELSIF TG_OP = 'INSERT' THEN
		INSERT INTO "calendar"."history_events" ("calendar_id", "event_id", "change_timestamp", "change_type")
		VALUES (NEW."calendar_id", NEW."event_id", NEW."revision_timestamp", 'C');
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
		IF NEW."calendar_id" <> OLD."calendar_id" THEN
			INSERT INTO "calendar"."history_events" ("calendar_id", "event_id", "change_timestamp", "change_type")
			VALUES
			(OLD."calendar_id", NEW."event_id", NEW."revision_timestamp", 'D'),
			(NEW."calendar_id", NEW."event_id", NEW."revision_timestamp", 'C');
		ELSIF NEW."revision_status" <> OLD."revision_status" AND NEW."revision_status" = 'D' THEN
			INSERT INTO "calendar"."history_events" ("calendar_id", "event_id", "change_timestamp", "change_type")
			VALUES (NEW."calendar_id", NEW."event_id", NEW."revision_timestamp", 'D');
		ELSE
			INSERT INTO "calendar"."history_events" ("calendar_id", "event_id", "change_timestamp", "change_type")
			VALUES (NEW."calendar_id", NEW."event_id", NEW."revision_timestamp", 'U');
		END IF;
		RETURN NEW;
	END IF;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;