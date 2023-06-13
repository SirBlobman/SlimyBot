CREATE TABLE IF NOT EXISTS `known_members` (
    `id` VARCHAR(32) PRIMARY KEY,
    `guild_id` VARCHAR(32) NOT NULL,
    `username` VARCHAR(64) DEFAULT NULL,
    `display` VARCHAR(64) DEFAULT `username`,
    `avatar_url` TEXT DEFAULT NULL
);
