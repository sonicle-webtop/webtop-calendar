@DataSource[default@com.sonicle.webtop.calendar]

CREATE OR REPLACE FUNCTION "calendar"."prune_calendars_history_on"()
  RETURNS "pg_catalog"."trigger" AS $BODY$BEGIN
	IF TG_OP = 'INSERT' THEN
		DELETE FROM "calendar"."history_calendars" WHERE "calendar_id" = NEW."calendar_id" AND "change_timestamp" < NEW."change_timestamp" AND "change_type" = NEW."change_type";
		RETURN NEW;
	END IF;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100