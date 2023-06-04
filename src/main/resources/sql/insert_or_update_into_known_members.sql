INSERT INTO `known_members`
(`id`, `guild_id`, `name`, `tag`, `avatar_url`)
VALUES (?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`tag` = VALUES(`tag`),
`avatar_url` = VALUES(`avatar_url`);
