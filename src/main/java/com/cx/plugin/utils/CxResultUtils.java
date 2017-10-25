package com.cx.plugin.utils;

import com.cx.client.dto.ScanResults;
import com.cx.client.rest.dto.OSAScanStatus;
import com.cx.client.rest.dto.OSASummaryResults;
import com.cx.plugin.dto.CxResultsConst;
import com.cx.plugin.dto.CxScanConfig;
import com.cx.plugin.dto.CxXMLResults;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import static com.cx.plugin.dto.CxParam.OPTION_FALSE;
import static com.cx.plugin.dto.CxParam.OPTION_TRUE;
import static com.cx.plugin.dto.CxResultsConst.*;

/**
 * Created by Galn on 23/02/2017.
 */
public abstract class CxResultUtils {
    public static final Logger log = LoggerFactory.getLogger(CxResultUtils.class);

    public static String resolveCostumeBuildData(Map<String, String> customBuildData) {
        String resultsTemplate = getResultsTemplate();
        if (resultsTemplate == null) {
            return "";
        }

        String ret = resultsTemplate;

        if (customBuildData.get(SAST_RESULTS_READY) != null) {

            //SAST: fill html with results
            ret = ret
                    .replace(SAST_RESULTS_READY, OPTION_TRUE)
                    .replace(SAST_SYNC_MODE, String.valueOf(customBuildData.get(SAST_SYNC_MODE)))
                    .replaceAll(HIGH_RESULTS, String.valueOf(customBuildData.get(HIGH_RESULTS)))
                    .replace(MEDIUM_RESULTS, String.valueOf(customBuildData.get(MEDIUM_RESULTS)))
                    .replace(LOW_RESULTS, String.valueOf(customBuildData.get(LOW_RESULTS)))
                    .replace(SAST_SUMMARY_RESULTS_LINK, String.valueOf(customBuildData.get(SAST_SUMMARY_RESULTS_LINK)))
                    .replace(SAST_SCAN_RESULTS_LINK, String.valueOf(customBuildData.get(SAST_SCAN_RESULTS_LINK)))
                    .replace(THRESHOLD_ENABLED, String.valueOf(customBuildData.get(THRESHOLD_ENABLED)))
                    .replace(HIGH_THRESHOLD, String.valueOf(customBuildData.get(HIGH_THRESHOLD)))
                    .replace(MEDIUM_THRESHOLD, String.valueOf(customBuildData.get(MEDIUM_THRESHOLD)))
                    .replace(LOW_THRESHOLD, String.valueOf(customBuildData.get(LOW_THRESHOLD)))
                    .replace(SCAN_START_DATE, String.valueOf(customBuildData.get(SCAN_START_DATE)))
                    .replace(SCAN_TIME, String.valueOf(customBuildData.get(SCAN_TIME)))
                    .replace(SCAN_FILES_SCANNED, String.valueOf(customBuildData.get(SCAN_FILES_SCANNED)))
                    .replace(SCAN_LOC_SCANNED, String.valueOf(customBuildData.get(SCAN_LOC_SCANNED)))
                    .replace(SCAN_QUERY_LIST, String.valueOf(customBuildData.get(SCAN_QUERY_LIST)));
        } else {

            //SAST: fill html with empty values
            ret = ret
                    .replace(SAST_RESULTS_READY, OPTION_FALSE)
                    .replace(SAST_SYNC_MODE, String.valueOf(customBuildData.get(SAST_SYNC_MODE)))
                    .replace(SAST_SUMMARY_RESULTS_LINK, StringUtils.defaultString(customBuildData.get(SAST_SUMMARY_RESULTS_LINK)))
                    .replaceAll(HIGH_RESULTS, "0")
                    .replace(MEDIUM_RESULTS, "0")
                    .replace(LOW_RESULTS, "0")
                    .replace(SAST_SCAN_RESULTS_LINK, "")
                    .replace(THRESHOLD_ENABLED, OPTION_FALSE)
                    .replace(HIGH_THRESHOLD, "0")
                    .replace(MEDIUM_THRESHOLD, "0")
                    .replace(LOW_THRESHOLD, "0")
                    .replace(SCAN_START_DATE, "")
                    .replace(SCAN_TIME, "")
                    .replace(SCAN_FILES_SCANNED, "null")
                    .replace(SCAN_LOC_SCANNED, "null")
                    .replace(SCAN_QUERY_LIST, "null");
        }

        if (customBuildData.get(OSA_RESULTS_READY) != null) {
            //OSA: fill html with results
            ret = ret
                    .replace(OSA_ENABLED, OPTION_TRUE)
                    .replace(OSA_HIGH_RESULTS, String.valueOf(customBuildData.get(OSA_HIGH_RESULTS)))
                    .replace(OSA_MEDIUM_RESULTS, String.valueOf(customBuildData.get(OSA_MEDIUM_RESULTS)))
                    .replace(OSA_LOW_RESULTS, String.valueOf(customBuildData.get(OSA_LOW_RESULTS)))
                    .replace(OSA_SUMMARY_RESULTS_LINK, String.valueOf(customBuildData.get(OSA_SUMMARY_RESULTS_LINK)))
                    .replace(OSA_THRESHOLD_ENABLED, String.valueOf(customBuildData.get(OSA_THRESHOLD_ENABLED)))
                    .replace(OSA_HIGH_THRESHOLD, String.valueOf(customBuildData.get(OSA_HIGH_THRESHOLD)))
                    .replace(OSA_MEDIUM_THRESHOLD, String.valueOf(customBuildData.get(OSA_MEDIUM_THRESHOLD)))
                    .replace(OSA_LOW_THRESHOLD, String.valueOf(customBuildData.get(OSA_LOW_THRESHOLD)))
                    .replace(OSA_VULNERABLE_LIBRARIES, String.valueOf(customBuildData.get(OSA_VULNERABLE_LIBRARIES)))
                    .replace(OSA_OK_LIBRARIES, String.valueOf(customBuildData.get(OSA_OK_LIBRARIES)))
                    .replace(OSA_CVE_LIST, String.valueOf(customBuildData.get(OSA_CVE_LIST)))
                    .replace(OSA_LIBRARIES, String.valueOf(customBuildData.get(OSA_LIBRARIES)))
                    .replace(OSA_START_TIME, String.valueOf(customBuildData.get(OSA_START_TIME)))
                    .replace(OSA_END_TIME, String.valueOf(customBuildData.get(OSA_END_TIME)));

        } else {

            //SAST: fill html with empty values
            ret = ret
                    .replace(OSA_ENABLED, OPTION_FALSE)
                    .replace(OSA_HIGH_RESULTS, "0")
                    .replace(OSA_MEDIUM_RESULTS, "0")
                    .replace(OSA_LOW_RESULTS, "0")
                    .replace(OSA_SUMMARY_RESULTS_LINK, "")
                    .replace(OSA_THRESHOLD_ENABLED, OPTION_FALSE)
                    .replace(OSA_HIGH_THRESHOLD, "0")
                    .replace(OSA_MEDIUM_THRESHOLD, "0")
                    .replace(OSA_LOW_THRESHOLD, "0")
                    .replace(OSA_VULNERABLE_LIBRARIES, "0")
                    .replace(OSA_OK_LIBRARIES, "0")
                    .replace(OSA_CVE_LIST, "null")
                    .replace(OSA_LIBRARIES, "null")
                    .replace(OSA_START_TIME, "")
                    .replace(OSA_END_TIME, "");
        }

        return ret;
    }

    private static String getResultsTemplate() {
        String ret = null;
        InputStream resourceAsStream = CxResultUtils.class.getResourceAsStream("/com/cx/plugin/resultsTemplate.html");
        if (resourceAsStream != null) {
            try {
                ret = IOUtils.toString(resourceAsStream, Charset.defaultCharset().name());
            } catch (IOException e) {
                log.warn("Failed to get results template", e.getMessage());
            } finally {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    log.warn("Failed to close streams", e.getMessage());
                }
            }
        }
        return ret;
    }

    public static void addSASTResults(Map<String, String> results, ScanResults scanResults, CxScanConfig config, String projectStateLink, String scanResultsUrl) {
        results.put(CxResultsConst.HIGH_RESULTS, String.valueOf(scanResults.getHighSeverityResults()));
        results.put(CxResultsConst.MEDIUM_RESULTS, String.valueOf(scanResults.getMediumSeverityResults()));
        results.put(CxResultsConst.LOW_RESULTS, String.valueOf(scanResults.getLowSeverityResults()));
        results.put(CxResultsConst.SAST_SUMMARY_RESULTS_LINK, org.apache.commons.lang.StringUtils.defaultString(projectStateLink));
        results.put(CxResultsConst.SAST_SCAN_RESULTS_LINK, org.apache.commons.lang.StringUtils.defaultString(scanResultsUrl));
        results.put(CxResultsConst.THRESHOLD_ENABLED, String.valueOf(config.isSASTThresholdEnabled()));

        if (config.isThresholdsEnabled()) {
            String highThreshold = (config.getHighThreshold() == null ? "null" : String.valueOf(config.getHighThreshold()));
            String mediumThreshold = (config.getMediumThreshold() == null ? "null" : String.valueOf(config.getMediumThreshold()));
            String lowThreshold = (config.getLowThreshold() == null ? "null" : String.valueOf(config.getLowThreshold()));

            results.put(CxResultsConst.HIGH_THRESHOLD, highThreshold);
            results.put(CxResultsConst.MEDIUM_THRESHOLD, mediumThreshold);
            results.put(CxResultsConst.LOW_THRESHOLD, lowThreshold);
        }


        results.put(CxResultsConst.SCAN_START_DATE, String.valueOf(scanResults.getScanStart()));
        results.put(CxResultsConst.SCAN_TIME, String.valueOf(scanResults.getScanTime()));
        results.put(CxResultsConst.SCAN_FILES_SCANNED, String.valueOf(scanResults.getFilesScanned()));
        results.put(CxResultsConst.SCAN_LOC_SCANNED, String.valueOf(scanResults.getLinesOfCodeScanned()));
        results.put(CxResultsConst.SCAN_QUERY_LIST, String.valueOf(scanResults.getQueryList()));

        results.put(CxResultsConst.SAST_RESULTS_READY, OPTION_TRUE);

    }

    public static void addOSAResults(Map<String, String> results, OSASummaryResults osaSummaryResults, CxScanConfig config, String osaProjectSummaryLink) {

        results.put(CxResultsConst.OSA_HIGH_RESULTS, String.valueOf(osaSummaryResults.getTotalHighVulnerabilities()));
        results.put(CxResultsConst.OSA_MEDIUM_RESULTS, String.valueOf(osaSummaryResults.getTotalMediumVulnerabilities()));
        results.put(CxResultsConst.OSA_LOW_RESULTS, String.valueOf(osaSummaryResults.getTotalLowVulnerabilities()));
        results.put(CxResultsConst.OSA_SUMMARY_RESULTS_LINK, org.apache.commons.lang.StringUtils.defaultString(osaProjectSummaryLink));
        results.put(CxResultsConst.OSA_VULNERABLE_LIBRARIES, String.valueOf(osaSummaryResults.getHighVulnerabilityLibraries() + osaSummaryResults.getMediumVulnerabilityLibraries() + osaSummaryResults.getLowVulnerabilityLibraries()));
        results.put(CxResultsConst.OSA_OK_LIBRARIES, String.valueOf(osaSummaryResults.getNonVulnerableLibraries()));
        results.put(CxResultsConst.OSA_THRESHOLD_ENABLED, String.valueOf(config.isOSAThresholdEnabled()));

        if (config.isOSAThresholdEnabled()) {

            String osaHighThreshold = (config.getOsaHighThreshold() == null ? "null" : String.valueOf(config.getOsaHighThreshold()));
            String osaMediumThreshold = (config.getOsaMediumThreshold() == null ? "null" : String.valueOf(config.getOsaMediumThreshold()));
            String osaLowThreshold = (config.getOsaLowThreshold() == null ? "null" : String.valueOf(config.getOsaLowThreshold()));

            results.put(CxResultsConst.OSA_HIGH_THRESHOLD, osaHighThreshold);
            results.put(CxResultsConst.OSA_MEDIUM_THRESHOLD, osaMediumThreshold);
            results.put(CxResultsConst.OSA_LOW_THRESHOLD, osaLowThreshold);
        }

        results.put(CxResultsConst.OSA_RESULTS_READY, OPTION_TRUE);
    }

    public static void addOSAStatus(Map<String, String> results, OSAScanStatus osaScanStatus) {
        results.put(CxResultsConst.OSA_START_TIME, osaScanStatus.getStartAnalyzeTime());
        results.put(CxResultsConst.OSA_END_TIME, osaScanStatus.getEndAnalyzeTime());
    }

    public static void addOsaCveAndLibLists(Map<String, String> results, String osaVulnerabilities, String osaLibraries) {
        results.put(CxResultsConst.OSA_CVE_LIST, osaVulnerabilities);
        results.put(CxResultsConst.OSA_LIBRARIES, osaLibraries);

    }

    public static CxXMLResults convertToXMLResult(byte[] cxReport) throws IOException, JAXBException {

        CxXMLResults reportObj = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cxReport);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CxXMLResults.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            reportObj = (CxXMLResults) unmarshaller.unmarshal(byteArrayInputStream);

        } finally {
            IOUtils.closeQuietly(byteArrayInputStream);
        }
        return reportObj;
    }

}

