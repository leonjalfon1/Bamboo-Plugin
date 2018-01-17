package com.cx.plugin.utils;

import com.cx.client.CxClientService;
import com.cx.client.dto.ReportType;
import com.cx.client.exception.CxClientException;
import com.cx.client.osa.dto.CVE;
import com.cx.client.osa.dto.Library;
import com.cx.client.osa.dto.OSASummaryResults;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Galn on 25/10/2017.
 */
public class CxReportsUtils {
    private static final String PDF_REPORT_NAME = "CxSASTReport";
    private static final String CX_REPORT_LOCATION = File.separator + "Checkmarx" + File.separator + "Reports";
    private static final String OSA_REPORT_NAME = "CxOSAReport";
    public static final String OSA_LIBRARIES_NAME = "CxOSALibraries";
    public static final String OSA_VULNERABILITIES_NAME = "CxOSAVulnerabilities";
    public static final String OSA_SUMMARY_NAME = "CxOSASummary";

    private SimpleDateFormat ft = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
    private ObjectMapper objectMapper = new ObjectMapper();
    private String now = ft.format(new Date());

    //SAST PDF
    public void createPDFReport(long scanId, String workspace, CxLoggerAdapter log, CxClientService cxClientService) throws InterruptedException {
        log.info("Generating PDF report");
        byte[] scanReport;
        try {
            scanReport = cxClientService.getScanReport(scanId, ReportType.PDF);
            SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
            String now = df.format(new Date());
            String pdfFileName = PDF_REPORT_NAME + "_" + now + ".pdf";
            FileUtils.writeByteArrayToFile(new File(workspace + CX_REPORT_LOCATION, pdfFileName), scanReport);
            log.info("PDF report location: " + workspace + CX_REPORT_LOCATION + File.separator + pdfFileName);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fail to generate PDF report", e.getMessage());
        }
    }

    //OSA PDF report
    public void createOSAPDFReport(File workDirectory, String scanId, CxLoggerAdapter log, CxClientService cxClientService) throws IOException, CxClientException {
        byte[] osaPDF = cxClientService.retrieveOSAScanPDFResults(scanId);
        String pdfFileName = OSA_REPORT_NAME + "_" + now + ".pdf";
        FileUtils.writeByteArrayToFile(new File(workDirectory + CX_REPORT_LOCATION, pdfFileName), osaPDF);
        log.info("OSA PDF report location: " + workDirectory + CX_REPORT_LOCATION + File.separator + pdfFileName);
    }


    //OSA HTML report
    public void createOSAHTMLReport(File workDirectory, String scanId, CxLoggerAdapter log, CxClientService cxClientService) throws IOException, CxClientException {
        String osaHtml = cxClientService.retrieveOSAScanHtmlResults(scanId);
        String htmlFileName = OSA_REPORT_NAME + "_" + now + ".html";
        FileUtils.writeStringToFile(new File(workDirectory + CX_REPORT_LOCATION, htmlFileName), osaHtml, Charset.defaultCharset());
        log.info("OSA HTML report location: " + workDirectory + CX_REPORT_LOCATION + File.separator + htmlFileName);
        log.info("");
    }

    public void createOSASummaryJsonReport(File workDir, CxLoggerAdapter buildLoggerAdapter, OSASummaryResults osaSummaryResults) throws IOException, CxClientException {
        String fileName = OSA_SUMMARY_NAME + "_" + now + ".json";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(workDir + CX_REPORT_LOCATION, fileName), osaSummaryResults);
        buildLoggerAdapter.info("OSA summary json location: " + workDir + CX_REPORT_LOCATION + File.separator + fileName);
    }

    public List<Library> createOSALibrariesJsonReport(File workDirectory, String scanId, CxLoggerAdapter log, CxClientService cxClientService) throws IOException, CxClientException {
        List<Library> libraries = cxClientService.getOSALibraries(scanId);
        String fileName = OSA_LIBRARIES_NAME + "_" + now + ".json";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(workDirectory + CX_REPORT_LOCATION, fileName), libraries);
        log.info("OSA libraries json location: " + workDirectory + CX_REPORT_LOCATION + File.separator + fileName);

        return libraries;
    }

    public List<CVE> createOSAVulnerabilitiesJsonReport(File workDir, String scanId, CxLoggerAdapter log, CxClientService cxClientService) throws IOException, CxClientException {
        List<CVE> osaVulnerabilities = cxClientService.getOSAVulnerabilities(scanId);
        String fileName = OSA_VULNERABILITIES_NAME + "_" + now + ".json";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(workDir + CX_REPORT_LOCATION, fileName), osaVulnerabilities);
        log.info("OSA vulnerabilities json location: " + workDir + CX_REPORT_LOCATION + File.separator + fileName);
        return osaVulnerabilities;

    }

}
