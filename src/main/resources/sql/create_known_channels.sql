CREATE TABLE IF NOT EXISTS `known_channels` (
    `id` VARCHAR(32) PRIMARY KEY,
    `guild_id` VARCHAR(32),
    `name` VARCHAR(32),
    `type` VARCHAR(32)
);
