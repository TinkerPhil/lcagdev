CREATE TABLE IF NOT EXISTS `i7b0_userFundingSummary` (
  `uid` int(11) DEFAULT NULL,
  `lcagAmount` decimal(13,2) DEFAULT '0.00',
  `ffcAmount` decimal(13,2) DEFAULT '0.00',
  `isMember` bit(1) DEFAULT b'0',
  `isSuspended` bit(1) DEFAULT b'0',
  `lcagtxnCount` int(11) DEFAULT '0',
  `ffctxnCount` int(11) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;