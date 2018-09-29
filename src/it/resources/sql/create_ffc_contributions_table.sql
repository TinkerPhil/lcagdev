CREATE TABLE IF NOT EXISTS `i7b0_ffc_contributions` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned DEFAULT NULL,
  `username` varchar(120) DEFAULT NULL,
  `hash` varchar(120) DEFAULT NULL,
  `membership_token` varchar(120) DEFAULT NULL,
  `first_name` varchar(120) DEFAULT NULL,
  `last_name` varchar(120) DEFAULT NULL,
  `email_address` varchar(120) DEFAULT NULL,
  `address_line_1` varchar(500) DEFAULT NULL,
  `address_line_2` varchar(500) DEFAULT NULL,
  `city` varchar(200) DEFAULT NULL,
  `postal_code` varchar(50) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `gross_amount` decimal(13,2) NOT NULL,
  `invoice_created` int(10) unsigned NOT NULL DEFAULT '0',
  `payment_received` int(10) unsigned NOT NULL DEFAULT '0',
  `stripe_token` varchar(120) NOT NULL,
  `reference` varchar(120) NOT NULL,
  `status` varchar(120) NOT NULL,
  `error_description` varchar(200) DEFAULT NULL,
  `payment_type` varchar(20) NOT NULL,
  `payment_method` varchar(40) NOT NULL,
  `email_sent` tinyint(1) NOT NULL DEFAULT '0',
  `guid` varchar(120) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=48 DEFAULT CHARSET=utf8;