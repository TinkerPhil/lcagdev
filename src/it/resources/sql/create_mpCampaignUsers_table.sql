CREATE TABLE `i7b0_mpCampaignUsers` (
  `uid` int(11) NOT NULL,
  `mpId` int(11) DEFAULT NULL,
  `allowEmailShareStatus` varchar(20) DEFAULT 'Not Asked',
  `sentInitialEmail` varchar(1) DEFAULT 'N',
  `campaignNotes` mediumtext,
  `telNo` varchar(100) DEFAULT NULL,
  `meetingNext` datetime DEFAULT NULL,
  `meetingCount` int(11) DEFAULT NULL,
  `telephoneCount` int(11) DEFAULT NULL,
  `writtenCount` int(11) DEFAULT NULL,
  `involved` int(11) DEFAULT NULL,
  `tags` varchar(200) DEFAULT NULL,
  `signature` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;