package com.cx.plugin.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String ret = "";
        String resultsTemplate = getResultsTemplate();
        if (customBuildData.get(SAST_RESULTS_READY) != null) {
            if (resultsTemplate != null) {
                ret = resultsTemplate
                        .replace(SAST_RESULTS_READY, String.valueOf(customBuildData.get(SAST_RESULTS_READY)))
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
                        .replace(SCAN_QUERY_LIST, String.valueOf(customBuildData.get(SCAN_QUERY_LIST)))
                ;


                if (customBuildData.get(OSA_RESULTS_READY) != null) {
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
                            .replace(OSA_CVE_LIST, "")
                            .replace(OSA_LIBRARIES, "")
                            .replace(OSA_START_TIME, "")
                            .replace(OSA_END_TIME, "");
                }
            }

        } else if (customBuildData.get(OSA_RESULTS_READY) != null) {
            ret = resultsTemplate
                    .replace(SAST_RESULTS_READY, OPTION_FALSE)
                    .replaceAll(HIGH_RESULTS, "0")
                    .replace(MEDIUM_RESULTS, "0")
                    .replace(LOW_RESULTS, "0")
                    .replace(SAST_SUMMARY_RESULTS_LINK, "")
                    .replace(SAST_SCAN_RESULTS_LINK, "")
                    .replace(THRESHOLD_ENABLED, OPTION_FALSE)
                    .replace(HIGH_THRESHOLD, "0")
                    .replace(MEDIUM_THRESHOLD, "0")
                    .replace(LOW_THRESHOLD, "0")

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
                    .replace(OSA_OK_LIBRARIES, String.valueOf(customBuildData.get(OSA_OK_LIBRARIES)));

        } else {
            return "";
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
}

