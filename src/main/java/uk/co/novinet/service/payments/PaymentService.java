package uk.co.novinet.service.payments;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jcraft.jsch.ChannelSftp.LsEntrySelector.BREAK;
import static com.jcraft.jsch.ChannelSftp.LsEntrySelector.CONTINUE;
import static java.lang.Double.parseDouble;
import static java.nio.charset.Charset.forName;
import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.readLines;
import static uk.co.novinet.service.payments.ImportOutcome.FAILURE;
import static uk.co.novinet.service.payments.ImportOutcome.SUCCESS;

@Service
public class PaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "Date:.(?<date>\\d{2}/\\d{2}/\\d{4})\\s+" +
            "Description:(?<description>.+)\\s*" +
            "Amount:.(?<amount>\\d+\\.\\d{2}).\\s*" +
            "Balance:.(?<balance>\\d+\\.\\d{2})"
    );

    private static final List<Pattern> DESCRIPTION_PATTERNS = asList(
            Pattern.compile("FASTER PAYMENTS RECEIPT REF.(?<reference>.{0,18}) FROM (?<counterParty>.*)"),
            Pattern.compile("BILL PAYMENT FROM (?<counterParty>.*), REFERENCE (?<reference>.{0,18})")
    );

    @Value("${bankExportSftpUsername}")
    private String bankExportSftpUsername;

    @Value("${bankExportSftpPassword}")
    private String bankExportSftpPassword;

    @Value("${bankExportSftpHost}")
    private String bankExportSftpHost;

    @Value("${bankExportSftpPort}")
    private int bankExportSftpPort;

    @Value("${bankExportSftpTodoPath}")
    private String bankExportSftpTodoPath;

    @Value("${bankExportSftpSuccessPath}")
    private String bankExportSftpSuccessPath;

    @Value("${bankExportSftpFailurePath}")
    private String bankExportSftpFailurePath;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaymentDao paymentDao;

    @Scheduled(initialDelayString = "${pollBankExportFolderInitialDelayMilliseconds}", fixedRateString = "${pollBankExportFolderIntervalMilliseconds}")
    public void reconcileTransactions() {
        LOGGER.info("Checking for new bank transaction exports");

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession(bankExportSftpUsername, bankExportSftpHost, bankExportSftpPort);
            session.setPassword(bankExportSftpPassword);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.cd(bankExportSftpTodoPath);

            Vector<ChannelSftp.LsEntry> remoteTodoFiles = sftpChannel.ls(bankExportSftpTodoPath);

            LOGGER.info("Found these files to process: {}", remoteTodoFiles);

            if (remoteTodoFiles != null && remoteTodoFiles.size() > 0) {
                for (ChannelSftp.LsEntry lsEntry : remoteTodoFiles) {
                    if (lsEntry.getFilename().endsWith(".txt")) {
                        File localFile = File.createTempFile("lcag_transactions", "txt");
                        sftpChannel.get(lsEntry.getFilename(), new FileOutputStream(localFile));
                        processAndMove(sftpChannel, lsEntry, localFile);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void processAndMove(ChannelSftp channelSftp, ChannelSftp.LsEntry ftpFile, File localFile) {
        move(channelSftp, ftpFile, importTransactionsFromFile(localFile) == SUCCESS ? bankExportSftpSuccessPath : bankExportSftpFailurePath);
    }

    private void move(ChannelSftp channelSftp, ChannelSftp.LsEntry lsEntry, String destination) {
        try {
            channelSftp.rename("bankExportFtpTodoPath" + lsEntry.getFilename(), destination);
        } catch (SftpException e) {
            LOGGER.error("Could not move remote file {} from current directory to {}", lsEntry, destination, e);
            throw new RuntimeException(e);
        }

    }

    private ImportOutcome importTransactionsFromFile(File localFile) {
        try {
            List<String> lines = readLines(new FileInputStream(localFile), forName("iso-8859-1"));
            List<BankTransaction> bankTransactions = buildBankTransactions(lines.stream().collect(Collectors.joining("\n")));
            for (BankTransaction bankTransaction : bankTransactions) {
                if (paymentDao.findExistingBankTransaction(bankTransaction) == null || paymentDao.findExistingBankTransaction(bankTransaction).isEmpty()) {
                    paymentDao.create(bankTransaction);
                }
            }

            return SUCCESS;
        } catch (IOException e) {
            LOGGER.error("Could not read localFile {}", localFile, e);
            return FAILURE;
        }
    }

    List<BankTransaction> buildBankTransactions(String transactions) {
        Matcher matcher = TRANSACTION_PATTERN.matcher(transactions);

        List<BankTransaction> bankTransactions = new ArrayList<>();

        while (matcher.find()) {
            String date = matcher.group("date");
            String description = matcher.group("description").replace('\u00A0',' ').trim();
            String amount = matcher.group("amount");
            String balance = matcher.group("balance");
            try {
                bankTransactions.add(new BankTransaction(
                        0L,
                        null,
                        new SimpleDateFormat("dd/MM/yyyy").parse(date),
                        description,
                        parseDouble(amount),
                        parseDouble(balance),
                        findInDescription(description, "counterParty"),
                        findInDescription(description, "reference"))
                );
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        //oldest first
        Collections.reverse(bankTransactions);

        return bankTransactions;
    }

    private String findInDescription(String description, String groupName) {
        for (Pattern pattern : DESCRIPTION_PATTERNS) {
            Matcher matcher = pattern.matcher(description);
            if (matcher.find()) {
                return matcher.group(groupName);
            }
        }
        return null;
    }

}
