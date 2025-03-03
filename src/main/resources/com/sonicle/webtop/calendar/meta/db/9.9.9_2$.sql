@DataSource[default@com.sonicle.webtop.calendar]

CREATE OR REPLACE FUNCTION "calendar"."update_calendars_history"()
  RETURNS TRIGGER AS $BODY$BEGIN
	IF TG_OP = 'DELETE' THEN
		INSERT INTO "calendar"."history_calendars" ("domain_id", "user_id", "calendar_id", "change_timestamp", "change_type")
		VALUES (OLD."domain_id", OLD."user_id", OLD."calendar_id", NOW(), 'D');
		RETURN OLD;
	ELSIF TG_OP = 'INSERT' THEN
		INSERT INTO "calendar"."history_calendars" ("domain_id", "user_id", "calendar_id", "change_timestamp", "change_type")
		VALUES (NEW."domain_id", NEW."user_id", NEW."calendar_id", NEW."revision_timestamp", 'C');
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
		INSERT INTO "calendar"."history_calendars" ("domain_id", "user_id", "calendar_id", "change_timestamp", "change_type")
		VALUES (NEW."domain_id", NEW."user_id", NEW."calendar_id", NEW."revision_timestamp", 'U');
		RETURN NEW;
	END IF;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;