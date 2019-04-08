CREATE TABLE IF NOT EXISTS `i7b0_audit_events` (
 `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
 `date` int(10) unsigned NOT NULL DEFAULT '0',
 `user_id` int(10) unsigned NOT NULL,
 `event_name` varchar(500) NOT NULL DEFAULT '',
 `event_parameters` text,
 PRIMARY KEY (`id`),
 KEY `user_id` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;