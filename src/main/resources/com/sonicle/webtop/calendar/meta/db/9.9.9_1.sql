@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- New table: events_tags
-- ----------------------------
CREATE TABLE "calendar"."events_tags" (
"event_id" int4 NOT NULL,
"tag_id" varchar(22) NOT NULL
)
WITH (OIDS=FALSE)

;

CREATE INDEX "events_tags_ak1" ON "calendar"."events_tags" USING btree ("tag_id");
ALTER TABLE "calendar"."events_tags" ADD PRIMARY KEY ("event_id", "tag_id");
ALTER TABLE "calendar"."events_tags" ADD FOREIGN KEY ("tag_id") REFERENCES "core"."tags" ("tag_id") ON DELETE CASCADE ON UPDATE CASCADE;
