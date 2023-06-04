INSERT INTO `known_channels`
(`id`, `guild_id`, `name`, `type`)
VALUES (?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`type` = VALUES(`type`);
