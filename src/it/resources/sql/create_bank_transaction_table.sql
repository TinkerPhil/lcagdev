CREATE TABLE IF NOT EXISTS `i7b0_bank_transactions` (
 `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
 `user_id` int(10) unsigned DEFAULT NULL,
 `date` int(10) unsigned NOT NULL DEFAULT '0',
 `description` varchar(500) NOT NULL DEFAULT '',
 `amount` decimal(10,2) NOT NULL DEFAULT '0.00',
 `running_balance` decimal(10,2) NOT NULL DEFAULT '0.00',
 `counter_party` varchar(120) NOT NULL DEFAULT '',
 `reference` varchar(500) NOT NULL DEFAULT '',
 `transaction_index_on_day` int(10) unsigned NOT NULL DEFAULT '0',
 `payment_source` varchar(10) NOT NULL DEFAULT '',
 `email_address` varchar(100) NOT NULL DEFAULT '',
 `id_ffc_bank` int(11) DEFAULT NULL,
 `email_sent` tinyint(1) NOT NULL DEFAULT '0',
 `excludeFromMemberReconciliation` bit(1) DEFAULT b'0',
 PRIMARY KEY (`id`),
 KEY `user_id` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3179 DEFAULT CHARSET=utf8;