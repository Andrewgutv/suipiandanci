-- 锁屏背单词 - 数据库初始化脚本
-- 创建时间: 2026-03-13
-- 数据库: fragment_words

-- 设置客户端字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS fragment_words DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fragment_words;

-- ============================================
-- 1. 用户表 (可选，用于未来扩展)
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) COMMENT '密码（加密）',
    `phone` VARCHAR(20) COMMENT '手机号',
    `device_id` VARCHAR(100) UNIQUE COMMENT '设备ID（用于未登录用户）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 词库表
-- ============================================
CREATE TABLE IF NOT EXISTS `vocab` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '词库ID',
    `name` VARCHAR(50) NOT NULL COMMENT '词库名称（如：CET4、CET6、IELTS、TOEFL、GRE）',
    `word_count` INT DEFAULT 0 COMMENT '单词总数',
    `description` VARCHAR(200) COMMENT '词库描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词库表';

-- ============================================
-- 3. 单词表
-- ============================================
CREATE TABLE IF NOT EXISTS `word` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '单词ID',
    `word` VARCHAR(100) NOT NULL COMMENT '单词',
    `phonetic` VARCHAR(100) COMMENT '音标',
    `translation` TEXT NOT NULL COMMENT '翻译',
    `example` TEXT COMMENT '例句',
    `vocab_id` BIGINT NOT NULL COMMENT '所属词库ID',
    `difficulty` INT DEFAULT 0 COMMENT '难度等级（0-5）',
    `frequency` INT DEFAULT 0 COMMENT '词频（用于推荐算法）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_vocab_id` (`vocab_id`),
    INDEX `idx_word` (`word`),
    INDEX `idx_difficulty` (`difficulty`),
    FOREIGN KEY (`vocab_id`) REFERENCES `vocab`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单词表';

-- ============================================
-- 4. 生词本表
-- ============================================
CREATE TABLE IF NOT EXISTS `unknown_word` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '生词ID',
    `device_id` VARCHAR(100) NOT NULL COMMENT '设备ID',
    `user_id` BIGINT COMMENT '用户ID（登录后关联）',
    `word_id` BIGINT NOT NULL COMMENT '单词ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    `review_count` INT DEFAULT 0 COMMENT '复习次数',
    `last_review_time` DATETIME COMMENT '最后复习时间',
    UNIQUE KEY `uk_device_word` (`device_id`, `word_id`),
    UNIQUE KEY `uk_user_word` (`user_id`, `word_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`word_id`) REFERENCES `word`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生词本表';

-- ============================================
-- 5. 学习进度表 (艾宾浩斯遗忘曲线核心)
-- ============================================
CREATE TABLE IF NOT EXISTS `learning_progress` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学习记录ID',
    `device_id` VARCHAR(100) NOT NULL COMMENT '设备ID',
    `user_id` BIGINT COMMENT '用户ID（登录后关联）',
    `word_id` BIGINT NOT NULL COMMENT '单词ID',
    `stage` TINYINT DEFAULT 0 COMMENT '艾宾浩斯阶段（0-8）',
    `known_count` INT DEFAULT 0 COMMENT '认识次数',
    `unknown_count` INT DEFAULT 0 COMMENT '不认识次数',
    `next_review_time` DATETIME COMMENT '下次复习时间',
    `last_review_time` DATETIME COMMENT '最后复习时间',
    `is_mastered` BOOLEAN DEFAULT FALSE COMMENT '是否已掌握',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_device_word` (`device_id`, `word_id`),
    UNIQUE KEY `uk_user_word` (`user_id`, `word_id`),
    INDEX `idx_device_next_review` (`device_id`, `next_review_time`),
    INDEX `idx_user_next_review` (`user_id`, `next_review_time`),
    INDEX `idx_is_mastered` (`is_mastered`),
    FOREIGN KEY (`word_id`) REFERENCES `word`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习进度表（艾宾浩斯算法）';

-- ============================================
-- 6. 设备偏好表
-- ============================================
CREATE TABLE IF NOT EXISTS `device_preference` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '偏好ID',
    `device_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '设备ID',
    `vocab_id` BIGINT COMMENT '当前选中的词库ID',
    `daily_goal` INT DEFAULT 50 COMMENT '每日学习目标（单词数）',
    `notification_enabled` BOOLEAN DEFAULT TRUE COMMENT '是否开启通知',
    `sound_enabled` BOOLEAN DEFAULT TRUE COMMENT '是否开启发音',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_device_id` (`device_id`),
    FOREIGN KEY (`vocab_id`) REFERENCES `vocab`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备偏好表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入词库数据
INSERT INTO `vocab` (`id`, `name`, `word_count`, `description`) VALUES
(1, 'CET4', 4500, '大学英语四级核心词汇'),
(2, 'CET6', 2500, '大学英语六级核心词汇'),
(3, 'IELTS', 3500, '雅思核心词汇'),
(4, 'TOEFL', 4000, '托福核心词汇'),
(5, 'GRE', 8000, 'GRE核心词汇')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 插入示例单词（CET4）
INSERT INTO `word` (`word`, `phonetic`, `translation`, `example`, `vocab_id`, `difficulty`) VALUES
('abandon', '/əˈbændən/', 'v. 抛弃，放弃', 'He decided to abandon the project.', 1, 2),
('ability', '/əˈbɪləti/', 'n. 能力，才干', 'She has the ability to succeed.', 1, 1),
('abnormal', '/æbˈnɔːrml/', 'adj. 反常的，不正常的', 'The weather is abnormal this year.', 1, 3),
('aboard', '/əˈbɔːrd/', 'adv. 在船(车)上', 'Welcome aboard!', 1, 2),
('abolish', '/əˈbɑːlɪʃ/', 'v. 废除，取消', 'Slavery was abolished in 1865.', 1, 4),
('abortion', '/əˈbɔːrʃn/', 'n. 流产，堕胎', 'The topic of abortion is controversial.', 1, 4),
('about', '/əˈbaʊt/', 'prep. 关于，大约', 'Tell me about yourself.', 1, 1),
('above', '/əˈbʌv/', 'prep. 在...之上', 'The plane flew above the clouds.', 1, 1),
('abroad', '/əˈbrɔːd/', 'adv. 到国外，在国外', 'She is studying abroad.', 1, 2),
('absence', '/ˈæbsəns/', 'n. 缺席，不在场', 'His absence was noticed.', 1, 2)
ON DUPLICATE KEY UPDATE `word`=VALUES(`word`);

INSERT INTO `vocab` (`id`, `name`, `word_count`, `description`) VALUES
(6, 'GRADUATE', 8, 'Graduate exam sample vocabulary')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

INSERT INTO `word` (`word`, `phonetic`, `translation`, `example`, `vocab_id`, `difficulty`) VALUES
('abstain', '/əbˈsteɪn/', 'v. 弃权；戒绝', 'He chose to abstain from the vote.', 2, 4),
('adversity', '/ədˈvɜːsəti/', 'n. 逆境；困境', 'Adversity often reveals true character.', 2, 4),
('concise', '/kənˈsaɪs/', 'adj. 简洁的；简明的', 'Her answer was concise and accurate.', 2, 3),
('pervasive', '/pəˈveɪsɪv/', 'adj. 普遍的；无处不在的', 'Smartphones have a pervasive influence on daily life.', 2, 4),
('robust', '/rəʊˈbʌst/', 'adj. 强健的；稳健的', 'The team built a robust testing process.', 2, 3),
('tentative', '/ˈtentətɪv/', 'adj. 暂定的；试探性的', 'We made a tentative schedule for next week.', 2, 3),
('versatile', '/ˈvɜːsətaɪl/', 'adj. 多才多艺的；用途广泛的', 'A versatile vocabulary helps in formal writing.', 2, 3),
('yielding', '/ˈjiːldɪŋ/', 'adj. 易弯曲的；顺从的', 'The material is soft but not too yielding.', 2, 4)
ON DUPLICATE KEY UPDATE `word`=VALUES(`word`);

INSERT INTO `word` (`word`, `phonetic`, `translation`, `example`, `vocab_id`, `difficulty`) VALUES
('cohesion', '/kəʊˈhiːʒn/', 'n. 凝聚力；连贯性', 'Good cohesion makes an essay easier to follow.', 3, 4),
('feasible', '/ˈfiːzəbl/', 'adj. 可行的；切实可行的', 'The committee discussed whether the plan was feasible.', 3, 4),
('infrastructure', '/ˈɪnfrəstrʌktʃə(r)/', 'n. 基础设施', 'Rural infrastructure still needs investment.', 3, 4),
('literacy', '/ˈlɪtərəsi/', 'n. 读写能力；素养', 'Digital literacy is increasingly important.', 3, 3),
('metropolitan', '/ˌmetrəˈpɒlɪtən/', 'adj. 大都市的', 'Metropolitan transport systems are often crowded.', 3, 4),
('sustainable', '/səˈsteɪnəbl/', 'adj. 可持续的', 'The city wants a more sustainable energy policy.', 3, 3),
('tangible', '/ˈtændʒəbl/', 'adj. 有形的；切实的', 'The project delivered tangible benefits.', 3, 3),
('urbanization', '/ˌɜːbənaɪˈzeɪʃn/', 'n. 城市化', 'Rapid urbanization changed the region quickly.', 3, 4)
ON DUPLICATE KEY UPDATE `word`=VALUES(`word`);

INSERT INTO `word` (`word`, `phonetic`, `translation`, `example`, `vocab_id`, `difficulty`) VALUES
('biodiversity', '/ˌbaɪəʊdaɪˈvɜːsəti/', 'n. 生物多样性', 'The island is famous for its biodiversity.', 4, 4),
('catalyst', '/ˈkætəlɪst/', 'n. 催化剂；诱因', 'The policy became a catalyst for reform.', 4, 4),
('erosion', '/ɪˈrəʊʒn/', 'n. 侵蚀；腐蚀', 'Wind erosion damaged the farmland.', 4, 4),
('habitat', '/ˈhæbɪtæt/', 'n. 栖息地', 'Wetlands provide habitat for many birds.', 4, 3),
('isotope', '/ˈaɪsətəʊp/', 'n. 同位素', 'The lab measured the isotope in the sample.', 4, 5),
('sedimentary', '/ˌsedɪˈmentri/', 'adj. 沉积的', 'Sedimentary rocks preserve many fossils.', 4, 4),
('terrestrial', '/təˈrestriəl/', 'adj. 陆地的；地球的', 'The telescope compared marine and terrestrial climates.', 4, 4),
('turbulence', '/ˈtɜːbjələns/', 'n. 湍流；动荡', 'The aircraft entered a zone of turbulence.', 4, 4)
ON DUPLICATE KEY UPDATE `word`=VALUES(`word`);

INSERT INTO `word` (`word`, `phonetic`, `translation`, `example`, `vocab_id`, `difficulty`) VALUES
('abstruse', '/əbˈstruːs/', 'adj. 深奥难懂的', 'The article was too abstruse for beginners.', 5, 5),
('adamant', '/ˈædəmənt/', 'adj. 坚决的；固执不变的', 'She was adamant that the experiment needed more evidence.', 5, 5),
('austere', '/ɒˈstɪə(r)/', 'adj. 简朴严肃的；朴素的', 'The room had an austere but elegant design.', 5, 5),
('bolster', '/ˈbəʊlstə(r)/', 'v. 支持；加强', 'New data may bolster the author’s claim.', 5, 4),
('cacophony', '/kəˈkɒfəni/', 'n. 刺耳的杂音', 'The market was filled with a cacophony of voices.', 5, 5),
('deleterious', '/ˌdelɪˈtɪəriəs/', 'adj. 有害的', 'Sleep deprivation has deleterious effects on memory.', 5, 5),
('obdurate', '/ˈɒbdjərət/', 'adj. 顽固的；执拗的', 'The committee remained obdurate despite the criticism.', 5, 5),
('sagacious', '/səˈɡeɪʃəs/', 'adj. 睿智的；有远见的', 'Her sagacious advice prevented a costly mistake.', 5, 5)
ON DUPLICATE KEY UPDATE `word`=VALUES(`word`);

INSERT INTO `word` (`word`, `phonetic`, `translation`, `example`, `vocab_id`, `difficulty`) VALUES
('comprehend', '/ˌkɒmprɪˈhend/', 'v. 充分理解；领会', 'It takes time to comprehend the whole argument.', 6, 3),
('crucial', '/ˈkruːʃl/', 'adj. 至关重要的', 'Vocabulary retention is crucial for exam performance.', 6, 3),
('derive', '/dɪˈraɪv/', 'v. 得到；源于', 'Several conclusions derive from these early results.', 6, 3),
('empirical', '/ɪmˈpɪrɪkl/', 'adj. 以观察或实验为依据的', 'The paper offers strong empirical evidence.', 6, 4),
('formulate', '/ˈfɔːmjəleɪt/', 'v. 系统阐述；明确表达', 'You need to formulate a clearer research question.', 6, 4),
('inhibit', '/ɪnˈhɪbɪt/', 'v. 抑制；阻碍', 'Anxiety can inhibit language production.', 6, 4),
('notion', '/ˈnəʊʃn/', 'n. 概念；看法', 'The author challenges the notion of fixed ability.', 6, 3),
('subsequent', '/ˈsʌbsɪkwənt/', 'adj. 随后的；后来的', 'Subsequent chapters focus on practical examples.', 6, 4)
ON DUPLICATE KEY UPDATE `word`=VALUES(`word`);

UPDATE `vocab` v
SET v.word_count = (
    SELECT COUNT(*) FROM `word` w WHERE w.vocab_id = v.id
);

-- ============================================
-- 艾宾浩斯复习间隔说明（8个阶段）
-- ============================================
-- 阶段0: 新单词，首次学习
-- 阶段1: 5分钟后复习
-- 阶段2: 30分钟后复习
-- 阶段3: 12小时后复习
-- 阶段4: 1天后复习
-- 阶段5: 2天后复习
-- 阶段6: 4天后复习
-- 阶段7: 7天后复习
-- 阶段8: 15天后复习（掌握）
--
-- 用户点击"认识" → stage + 1，计算下次复习时间
-- 用户点击"不认识" → stage = 1（重置回5分钟后再复习）
-- stage = 8 且复习通过 → is_mastered = TRUE

-- ============================================
-- 查询统计SQL示例
-- ============================================
-- 查询某设备的学习统计
-- SELECT
--     COUNT(*) as total_words,
--     SUM(CASE WHEN is_mastered = TRUE THEN 1 ELSE 0 END) as mastered_count,
--     SUM(CASE WHEN is_mastered = FALSE THEN 1 ELSE 0 END) as learning_count,
--     ROUND(SUM(CASE WHEN is_mastered = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as mastery_rate
-- FROM learning_progress
-- WHERE device_id = 'xxx';

-- 查询需要复习的单词
-- SELECT w.*, lp.stage, lp.next_review_time
-- FROM learning_progress lp
-- JOIN word w ON lp.word_id = w.id
-- WHERE lp.device_id = 'xxx'
--   AND lp.is_mastered = FALSE
--   AND lp.next_review_time <= NOW()
-- ORDER BY lp.next_review_time ASC
-- LIMIT 10;
