CREATE TABLE IF NOT EXISTS `i7b0_bank_transactions_infull` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` datetime DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `moneyIn` decimal(13,2) DEFAULT NULL,
  `moneyOut` decimal(13,2) DEFAULT NULL,
  `rollingBalance` decimal(13,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1970 DEFAULT CHARSET=latin1;