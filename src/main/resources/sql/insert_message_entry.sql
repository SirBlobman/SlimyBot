INSERT INTO `message_history`
(`message_id`, `guild_id`, `channel_id`, `member_id`, `action_type`, `old_content`, `new_content`, `action_time`)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);
