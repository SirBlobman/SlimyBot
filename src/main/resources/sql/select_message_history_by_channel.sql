SELECT * FROM `message_history`
WHERE `guild_id`=?
AND `channel_id`=?
ORDER BY `action_time` ASC;
