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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Galn on 25/10/2017.
 */
public class CxReportsUtils {
    private static final String PDF_REPORT_NAME = "CxSASTReport";
    private static final String CX_REPORT_LOCATION = File.separator + "Checkmarx" + File.separator + "Reports";
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
            log.error("Failed to generate PDF report", e.getMessage());
        }
    }

    public void createOSASummaryJsonReport(File workDirectory, CxLoggerAdapter log, OSASummaryResults osaSummaryResults) throws IOException, CxClientException {
        writeJsonToFile(OSA_SUMMARY_NAME, osaSummaryResults, workDirectory, log);
    }

    public List<Library> createOSALibrariesJsonReport(File workDirectory, String scanId, CxLoggerAdapter log, CxClientService cxClientService) throws IOException, CxClientException {
        List<Library> libraries = cxClientService.getOSALibraries(scanId);
        writeJsonToFile(OSA_LIBRARIES_NAME, libraries, workDirectory, log);

        return libraries;
    }

    public List<CVE> createOSAVulnerabilitiesJsonReport(File workDirectory, String scanId, CxLoggerAdapter log, CxClientService cxClientService) throws IOException, CxClientException {
        List<CVE> osaVulnerabilities = cxClientService.getOSAVulnerabilities(scanId);
        writeJsonToFile(OSA_VULNERABILITIES_NAME, osaVulnerabilities, workDirectory,log);

        return osaVulnerabilities;
    }

    private void writeJsonToFile(String name, Object jsonObj, File workDirectory, CxLoggerAdapter log) throws IOException {
        String fileName = name + "_" + now + ".json";
        File jsonFile = new File(workDirectory + CX_REPORT_LOCATION, fileName);
        String json =  objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
        FileUtils.writeStringToFile(jsonFile, json);
        log.info(name + " json location: " + workDirectory + CX_REPORT_LOCATION + File.separator + fileName);

    }

}
