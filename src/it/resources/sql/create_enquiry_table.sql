CREATE TABLE IF NOT EXISTS `i7b0_enquiry` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(220) NOT NULL DEFAULT '',
  `email_address` varchar(220) NOT NULL DEFAULT '',
  `mp_name` varchar(200) NOT NULL DEFAULT '',
  `mp_constituency` varchar(200) NOT NULL DEFAULT '',
  `mp_party` varchar(200) NOT NULL DEFAULT '',
  `mp_engaged` tinyint(1) NOT NULL DEFAULT '0',
  `mp_sympathetic` tinyint(1) NOT NULL DEFAULT '0',
  `schemes` varchar(200) NOT NULL DEFAULT '',
  `industry` varchar(200) NOT NULL DEFAULT '',
  `how_did_you_hear_about_lcag` varchar(200) NOT NULL DEFAULT '',
  `member_of_big_group` tinyint(1) NOT NULL DEFAULT '0',
  `big_group_username` varchar(200) NOT NULL DEFAULT '',
  `has_been_processed` tinyint(1) NOT NULL DEFAULT '0',
  `date_created` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=194 DEFAULT CHARSET=utf8;
