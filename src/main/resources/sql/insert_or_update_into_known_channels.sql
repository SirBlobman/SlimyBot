INSERT INTO `known_channels`
    (`id`, `guild_id`, `name`, `type`)
VALUES
    (?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    `guild_id` = VALUES(`guild_id`),
    `name` = VALUES(`name`),
    `type` = VALUES(`type`)
;
