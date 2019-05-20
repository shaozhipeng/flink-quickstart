CREATE TABLE `t_pojo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `astyle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `time_start` tt NULL DEFAULT NULL,
  `time_end` tt NULL DEFAULT NULL,
  `sum_energy` decimal(15,2) DEFAULT NULL,
  `cnt` int(16) DEFAULT NULL,
  `avg_age` int(16) DEFAULT NULL,
  `day_date` date DEFAULT NULL,
  `topic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `group_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;