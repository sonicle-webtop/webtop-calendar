@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Update structure for events (required in order to fix zpush invitation)
-- ----------------------------
ALTER TABLE "calendar"."events" ADD COLUMN "handle_invitation" bool DEFAULT false;
CREATE INDEX "events_ak99" ON "calendar"."events" ("handle_invitation");
