@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- New table: events_tags
-- ----------------------------
CREATE TABLE "calendar"."events_tags" (
"event_id" int4 NOT NULL,
"tag_id" varchar(22) NOT NULL
);

CREATE INDEX "events_tags_ak1" ON "calendar"."events_tags" USING btree ("tag_id");
ALTER TABLE "calendar"."events_tags" ADD PRIMARY KEY ("event_id", "tag_id");
ALTER TABLE "calendar"."events_tags" ADD FOREIGN KEY ("tag_id") REFERENCES "core"."tags" ("tag_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- New table: events_custom_values
-- ----------------------------
CREATE TABLE "calendar"."events_custom_values" (
"event_id" int4 NOT NULL,
"custom_field_id" varchar(22) NOT NULL,
"string_value" varchar(255),
"number_value" float8,
"boolean_value" bool,
"date_value" timestamptz(6),
"text_value" text
);

ALTER TABLE "calendar"."events_custom_values" ADD PRIMARY KEY ("event_id", "custom_field_id");
ALTER TABLE "calendar"."events_custom_values" ADD FOREIGN KEY ("event_id") REFERENCES "calendar"."events" ("event_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "calendar"."events_custom_values" ADD FOREIGN KEY ("custom_field_id") REFERENCES "core"."custom_fields" ("custom_field_id") ON DELETE CASCADE ON UPDATE CASCADE;
