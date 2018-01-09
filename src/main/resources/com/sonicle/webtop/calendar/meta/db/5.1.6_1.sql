@DataSource[default@com.sonicle.webtop.calendar]

-- ----------------------------
-- Table structure for calendar_props
-- ----------------------------
DROP TABLE IF EXISTS "calendar"."calendar_props";
CREATE TABLE "calendar"."calendar_props" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"calendar_id" int4 NOT NULL,
"hidden" bool,
"color" varchar(20),
"sync" varchar(1)
)
WITH (OIDS=FALSE)
;

-- ----------------------------
-- Indexes structure for table calendar_props
-- ----------------------------
CREATE INDEX "calendar_props_ak1" ON "calendar"."calendar_props" USING btree ("calendar_id");

-- ----------------------------
-- Primary Key structure for table calendar_props
-- ----------------------------
ALTER TABLE "calendar"."calendar_props" ADD PRIMARY KEY ("domain_id", "user_id", "calendar_id");

-- ----------------------------
-- Data move: user_settings -> category_props
-- ----------------------------
INSERT INTO "calendar"."calendar_props" 
("domain_id", "user_id", "calendar_id", "hidden", "color") 
SELECT domain_id, user_id, 
CAST (replace(replace("key",'calendar.folder.data@',''),',','') AS int4), 
CASE WHEN split_part(split_part(substr("value",2,length("value")-2),',',1),':',2)='true' THEN TRUE ELSE NULL END, 
CASE WHEN replace(split_part(split_part(substr("value",2,length("value")-2),',',2),':',2),'"','')='null' THEN NULL ELSE replace(split_part(split_part(substr("value",2,length("value")-2),',',2),':',2),'"','') END 
FROM "core"."user_settings" 
WHERE "service_id" = 'com.sonicle.webtop.calendar' AND "key" LIKE 'calendar.folder.data@%';

-- ----------------------------
-- Cleanup possibly orphans
-- ----------------------------
DELETE FROM "calendar"."calendar_props" WHERE "calendar_id" NOT IN (SELECT "calendar_id" from "calendar"."calendars");

-- ----------------------------
-- Cleanup old settings
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.calendar') AND ("user_settings"."key" LIKE 'calendar.folder.data@%');
