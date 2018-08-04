package uk.co.novinet.service.enquiry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberCreationResult;
import uk.co.novinet.service.member.MemberService;

import java.util.Arrays;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Service
public class EnquiryTableListenerService {
    private static final List<Boolean> SEEN_FLAG_STATES = Arrays.asList(TRUE, FALSE);

    private static final Logger LOGGER = LoggerFactory.getLogger(EnquiryTableListenerService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private EnquiryDao enquiryDao;

    public void processNewEnquiryTableRows() {
        LOGGER.info("Checking for new enquiry table rows");

        try {
            List<Enquiry> enquiries = enquiryDao.getUnprocessed();

            LOGGER.info("Found {} enquiries to process", enquiries.size());

            for (Enquiry enquiry : enquiries) {
                LOGGER.info("Processing enquiry: {}", enquiry);

                MemberCreationResult memberCreationResult = memberService.createForumUserIfNecessary(enquiry);
                Member member = memberCreationResult.getMember();

                if (memberCreationResult.memberAlreadyExisted() && !member.alreadyHaveAnLcagAccountEmailSent()) {
                    LOGGER.info("We already have a member with email address: {} and we haven't already sent them the chaser email. Going to send accountAlreadyExistsEmail...", enquiry.getEmailAddress());
                    mailSenderService.sendAccountAlreadyExistsEmail(member);
                    memberService.markAsAlreadyHaveAnLcagAccountEmailSent(member);
                    enquiryDao.markAsProcessed(enquiry.getId());
                } else if (!memberCreationResult.memberAlreadyExisted()) {
                    LOGGER.info("We created a new member with email address: {}. Going to send followUpEmail...", enquiry.getEmailAddress());
                    mailSenderService.sendFollowUpEmail(member);
                    enquiryDao.markAsProcessed(enquiry.getId());
                } else {
                    enquiryDao.markAsProcessed(enquiry.getId());
                    LOGGER.info("Skipping email from: {} as they already have an account and we've already sent them the alreadyHaveAnLcagAccountEmail", enquiry.getEmailAddress());
                }
            }
            LOGGER.info("Finished checking enquiry. Going back to sleep now.");
        } catch (Exception e) {
            LOGGER.error("An error occurred while trying to read emails", e);
        }
    }
}
