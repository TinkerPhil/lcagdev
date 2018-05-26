package uk.co.novinet.service.payments;

import org.apache.commons.io.IOUtils;
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
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;
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

    @Value("${bankExportFtpUsername}")
    private String bankExportFtpUsername;

    @Value("${bankExportFtpPassword}")
    private String bankExportFtpPassword;

    @Value("${bankExportFtpHost}")
    private String bankExportFtpHost;

    @Value("${bankExportFtpPort}")
    private int bankExportFtpPort;

    @Value("${bankExportFtpTodoPath}")
    private String bankExportFtpTodoPath;

    @Value("${bankExportFtpSuccessPath}")
    private String bankExportFtpSuccessPath;

    @Value("${bankExportFtpFailurePath}")
    private String bankExportFtpFailurePath;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(initialDelayString = "${pollBankExportFolderInitialDelayMilliseconds}", fixedRateString = "${pollBankExportFolderIntervalMilliseconds}")
    public void reconcileTransactions() {
        LOGGER.info("Checking for new bank transaction exports");

        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(bankExportFtpHost, bankExportFtpPort);
            ftpClient.login(bankExportFtpUsername, bankExportFtpPassword);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile[] remoteTodoFiles = ftpClient.listFiles(bankExportFtpTodoPath, file -> file.isFile() && file.getName().endsWith(".txt"));

            LOGGER.info("Found these files to process: {}", remoteTodoFiles);

            if (remoteTodoFiles != null && remoteTodoFiles.length > 0) {
                ftpClient.changeWorkingDirectory(bankExportFtpTodoPath);
                for (FTPFile ftpFile : remoteTodoFiles) {
                    File localFile = File.createTempFile("lcag_transactions", "txt");
                    ftpClient.retrieveFile(ftpFile.getName(), new FileOutputStream(localFile));
                    processAndMove(ftpClient, ftpFile, localFile);
                }
            }


            // APPROACH #1: using retrieveFile(String, OutputStream)
            String remoteFile1 = "/test/video.mp4";
            File downloadFile1 = new File("D:/Downloads/video.mp4");
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();

            if (success) {
                System.out.println("File #1 has been downloaded successfully.");
            }

            // APPROACH #2: using InputStream retrieveFileStream(String)
            String remoteFile2 = "/test/song.mp3";
            File downloadFile2 = new File("D:/Downloads/song.mp3");
            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile2);
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream2.write(bytesArray, 0, bytesRead);
            }

            success = ftpClient.completePendingCommand();
            if (success) {
                System.out.println("File #2 has been downloaded successfully.");
            }
            outputStream2.close();
            inputStream.close();

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void processAndMove(FTPClient ftpClient, FTPFile ftpFile, File localFile) {
        move(ftpClient, ftpFile, importTransactionsFromFile(localFile) == SUCCESS ? bankExportFtpSuccessPath : bankExportFtpFailurePath);
    }

    private void move(FTPClient ftpClient, FTPFile ftpFile, String destination) {
        try {
            ftpClient.rename(ftpFile.getName(), destination);
        } catch (IOException e) {
            LOGGER.error("Could not move ftpFile {} from current directory to {}", ftpFile, destination, e);
            throw new RuntimeException(e);
        }

    }

    private ImportOutcome importTransactionsFromFile(File localFile) {
        try {
            return importTransactions(IOUtils.readLines(new FileInputStream(localFile), Charset.forName("iso-8859-1")).stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            LOGGER.error("Could not read localFile {}", localFile, e);
            return FAILURE;
        }
    }

    ImportOutcome importTransactions(String transactions) {
        return SUCCESS;
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
                        new SimpleDateFormat("dd/MM/yyyy").parse(date),
                        description,
                        parseDouble(amount),
                        parseDouble(balance),
                        find(description, "counterParty"),
                        find(description, "reference"))
                );
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return bankTransactions;
    }

    /*
    From: 26/11/2017 to 26/05/2018

Account: XXXX XXXX XXXX 0057

Description:  FASTER PAYMENTS RECEIPT REF.ABC123 FROM A Smith
Description:  FASTER PAYMENTS RECEIPT REF.BOBWINKS FROM JONES TD
Description:  FASTER PAYMENTS RECEIPT REF.KDMP FROM WILLIAMS MICHAEL
Description: BILL PAYMENT FROM MR JAMES ANDREW HARRISON SMYTHE, REFERENCE jim65
     */

    private String find(String description, String groupName) {
        for (Pattern pattern : DESCRIPTION_PATTERNS) {
            Matcher matcher = pattern.matcher(description);
            if (matcher.find()) {
                return matcher.group(groupName);
            }
        }
        return null;
    }

}
