CREATE TABLE IF NOT EXISTS `i7b0_mp_details` (
  `mp_id` int(11) NOT NULL,
  `mp_last_name` varchar(100) DEFAULT NULL,
  `mp_first_name` varchar(100) DEFAULT NULL,
  `mp_party` varchar(50) DEFAULT NULL,
  `mp_twitter` varchar(50) DEFAULT NULL,
  `mp_email` varchar(100) DEFAULT NULL,
  `mp_constituency` varchar(100) DEFAULT NULL,
  `mp_constituency_address` varchar(200) DEFAULT NULL,
  `edm_status` varchar(1) DEFAULT NULL,
  `ministerial_status` varchar(200) DEFAULT NULL,
  `url` varchar(200) DEFAULT NULL,
  `majority` int(11) DEFAULT NULL,
  `pCon` varchar(20) DEFAULT NULL,
  `IsSystem` bit(1) DEFAULT NULL,
  `MpGroup` int(11) DEFAULT NULL,
  PRIMARY KEY (`mp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;