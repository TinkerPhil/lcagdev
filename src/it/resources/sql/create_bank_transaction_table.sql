CREATE TABLE IF NOT EXISTS `i7b0_bank_transactions` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned,
  `date` int(10) unsigned NOT NULL DEFAULT '0',
  `description` varchar(120) NOT NULL DEFAULT '',
  `amount` decimal(10,2) NOT NULL DEFAULT '0',
  `running_balance` decimal(10,2) NOT NULL DEFAULT '0',
  `counter_party` varchar(120) NOT NULL DEFAULT '',
  `reference` varchar(120) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `i7b0_user`(`uid`)
) ENGINE=MyISAM AUTO_INCREMENT=194 DEFAULT CHARSET=utf8;
