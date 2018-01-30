@DataSource[default@com.sonicle.webtop.calendar]

ALTER TABLE "calendar"."events"
ADD COLUMN "handle_invitation" bool DEFAULT false;
