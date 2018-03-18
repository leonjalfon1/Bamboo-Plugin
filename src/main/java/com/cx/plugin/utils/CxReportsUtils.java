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
public abstract class CxReportsUtils {
    private static final String PDF_REPORT_NAME = "CxSASTReport";
    private static final String CX_REPORT_LOCATION = File.separator + "Checkmarx" + File.separator + "Reports";
    private static final String OSA_LIBRARIES_NAME = "CxOSALibraries";
    private static final String OSA_VULNERABILITIES_NAME = "CxOSAVulnerabilities";
    private static final String OSA_SUMMARY_NAME = "CxOSASummary";


    //SAST PDF
    public static void createPDFReport(long scanId, String workspace, CxLoggerAdapter log, CxClientService cxClientService) throws InterruptedException {
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

    public static void createOSASummaryJsonReport(File workDirectory, CxLoggerAdapter log, OSASummaryResults osaSummaryResults) throws IOException, CxClientException {
        writeJsonToFile(OSA_SUMMARY_NAME, osaSummaryResults, workDirectory, log);
    }

    public static List<Library> createOSALibrariesJsonReport(File workDirectory, String scanId, CxLoggerAdapter log, CxClientService cxClientService) throws IOException, CxClientException {
        List<Library> libraries = cxClientService.getOSALibraries(scanId);
        writeJsonToFile(OSA_LIBRARIES_NAME, libraries, workDirectory, log);

        return libraries;
    }

    public static List<CVE> createOSAVulnerabilitiesJsonReport(File workDirectory, String scanId, CxLoggerAdapter log, CxClientService cxClientService) throws IOException, CxClientException {
        List<CVE> osaVulnerabilities = cxClientService.getOSAVulnerabilities(scanId);
        writeJsonToFile(OSA_VULNERABILITIES_NAME, osaVulnerabilities, workDirectory, log);

        return osaVulnerabilities;
    }

    private static void writeJsonToFile(String name, Object jsonObj, File workDirectory, CxLoggerAdapter log) throws IOException {
        SimpleDateFormat ft = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
        ObjectMapper objectMapper = new ObjectMapper();
        String now = ft.format(new Date());
        String fileName = name + "_" + now + ".json";
        File jsonFile = new File(workDirectory + CX_REPORT_LOCATION, fileName);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
        FileUtils.writeStringToFile(jsonFile, json);
        log.info(name + " json location: " + workDirectory + CX_REPORT_LOCATION + File.separator + fileName);
    }

    public static void writeOsaDependenciesJson(String osaDependenciesJson, File workDirectory, CxLoggerAdapter log) {
        try {
            File file = new File(workDirectory + CX_REPORT_LOCATION , "CxOSADependencies.json");
            FileUtils.writeStringToFile(file, osaDependenciesJson, Charset.defaultCharset());
            log.info("OSA dependencies json saved to file: ["+file.getAbsolutePath()+"]");
        } catch (Exception e) {
            log.info("Failed to save OSA dependencies json to file: " + e.getMessage());
        }

    }

}
