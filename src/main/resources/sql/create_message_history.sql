CREATE TABLE IF NOT EXISTS `message_history` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `message_id` VARCHAR(32),
    `guild_id` VARCHAR(32),
    `channel_id` VARCHAR(32),
    `member_id` VARCHAR(32),
    `action_type` VARCHAR(6),
    `old_content` TEXT,
    `new_content` TEXT,
    `action_time` TIMESTAMP DEFAULT NOW()
);
