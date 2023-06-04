CREATE TABLE IF NOT EXISTS `known_members` (
    `id` VARCHAR(32) PRIMARY KEY,
    `guild_id` VARCHAR(32),
    `name` VARCHAR(64),
    `tag` VARCHAR(64),
    `avatar_url` TEXT
);
