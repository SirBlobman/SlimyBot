INSERT INTO `known_members`
(`id`, `guild_id`, `username`, `display`, `avatar_url`)
VALUES (?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
`username` = VALUES(`username`),
`display` = VALUES(`display`),
`avatar_url` = VALUES(`avatar_url`);
