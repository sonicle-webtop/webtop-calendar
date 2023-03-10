@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Cleanup any migrated data for testing purposes
-- ----------------------------
DELETE FROM "core"."shares" WHERE "service_id" = 'com.sonicle.webtop.calendar' AND "key" = 'CALENDAR';
DELETE FROM "core"."roles_permissions" WHERE "service_id" = 'com.sonicle.webtop.calendar' AND "key" = 'CALENDAR_FOLDERITEMS@SHARE';
DELETE FROM "core"."roles_permissions" WHERE "service_id" = 'com.sonicle.webtop.calendar' AND "key" = 'CALENDAR_FOLDER@SHARE';

-- ----------------------------
-- Add new cols for data migration
-- ----------------------------
ALTER TABLE "core"."shares" ADD COLUMN "old_share_id" int4;
ALTER TABLE "core"."shares" ADD COLUMN "old_wildcard_share_id" int4;

-- ----------------------------
-- Convert shares on ROOT to new row wildcard instance, remembering old share_id
-- ----------------------------
INSERT INTO "core"."shares" ("user_uid", "service_id", "key", "instance", "old_share_id")
SELECT "user_uid", "service_id", 'CALENDAR', '*', "share_id"
FROM "core"."shares"
WHERE "service_id" = 'com.sonicle.webtop.calendar' AND "key" = 'CALENDAR@ROOT';

-- ----------------------------
-- Convert shares on FOLDER to new row, remembering old share_id
-- ----------------------------
INSERT INTO "core"."shares" ("user_uid", "service_id", "key", "instance", "old_share_id")
SELECT "user_uid", "service_id", 'CALENDAR', "instance", "share_id"
FROM "core"."shares"
WHERE "service_id" = 'com.sonicle.webtop.calendar' AND "key" = 'CALENDAR@FOLDER' AND "instance" <> '*';

-- ----------------------------
-- Merges shares on FOLDER with wildcard into wildcard rows (done before), remembering old wildcard_share_id
-- ----------------------------
UPDATE "core"."shares" SET
"old_wildcard_share_id" = sub."share_id"
FROM (
SELECT "user_uid", "share_id"
FROM "core"."shares"
WHERE "service_id" = 'com.sonicle.webtop.calendar' AND "key" = 'CALENDAR@FOLDER' AND "instance" = '*'
) AS sub
WHERE "shares"."user_uid" = sub."user_uid" AND "service_id" = 'com.sonicle.webtop.calendar' AND "key" = 'CALENDAR' AND "instance" = '*';

-- ----------------------------
-- Consolidate permissions (CALENDAR@SHARE_ROOT, CALENDAR@SHARE_FOLDER, CALENDAR@SHARE_ELEMENTS keys are deprecated)
-- ----------------------------
INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CALENDAR_FOLDERITEMS@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CALENDAR' AND rp."instance"::integer = sh."old_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.calendar' AND rp."key" = 'CALENDAR@SHARE_ELEMENTS';

INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CALENDAR_FOLDERITEMS@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CALENDAR' AND rp."instance"::integer = sh."old_wildcard_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.calendar' AND rp."key" = 'CALENDAR@SHARE_ELEMENTS';

INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CALENDAR_FOLDER@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CALENDAR' AND rp."instance"::integer = sh."old_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.calendar' AND rp."key" = 'CALENDAR@SHARE_FOLDER';

INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CALENDAR_FOLDER@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CALENDAR' AND rp."instance"::integer = sh."old_wildcard_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.calendar' AND rp."key" = 'CALENDAR@SHARE_FOLDER';

INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CALENDAR_FOLDER@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CALENDAR' AND rp."instance"::integer = sh."old_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.calendar' AND rp."key" = 'CALENDAR@SHARE_ROOT';

-- ----------------------------
-- Drop new cols for data migration
-- ----------------------------
ALTER TABLE "core"."shares" DROP COLUMN "old_share_id", DROP COLUMN "old_wildcard_share_id";

-- ----------------------------
-- Cleanup old settings
-- ----------------------------
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.calendar') AND ("user_settings"."key" = 'calendar.roots.inactive');
