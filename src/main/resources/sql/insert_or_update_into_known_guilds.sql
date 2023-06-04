INSERT INTO `known_guilds`
(`id`, `guild_name`)
VALUES (?, ?)
ON DUPLICATE KEY UPDATE
`guild_name` = VALUES(`guild_name`);
