package com.cx.plugin;

import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.plugin.web.model.WebPanel;
import com.cx.plugin.dto.CxResultsConst;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import static com.cx.plugin.dto.CxResultsConst.*;

/**
 * Created by: dorg.
 * Date: 01/02/2017.
 */

public class CxResultsWebPanel implements WebPanel {

    private static final Logger log = LoggerFactory.getLogger(CxResultsWebPanel.class);

    public String getHtml(Map<String, Object> map) {

        ChainResultsSummaryImpl a = (ChainResultsSummaryImpl) map.get("resultSummary");

        Map<String, String> customBuildData = a.getOrderedJobResultSummaries().get(0).getCustomBuildData();

        String ret = "";
        if (customBuildData.get(SAST_RESULTS_READY) != null) {
            String resultsTemplate = getResultsTemplate();
            if (resultsTemplate != null) {

                ret = resultsTemplate
                        .replaceAll(HIGH_RESULTS, String.valueOf(customBuildData.get(HIGH_RESULTS)))
                        .replace(MEDIUM_RESULTS, String.valueOf(customBuildData.get(MEDIUM_RESULTS)))
                        .replace(LOW_RESULTS, String.valueOf(customBuildData.get(LOW_RESULTS)))
                        .replace(THRESHOLD_ENABLED, String.valueOf(customBuildData.get(THRESHOLD_ENABLED)))
                        .replace(HIGH_THRESHOLD, String.valueOf(customBuildData.get(HIGH_THRESHOLD)))
                        .replace(MEDIUM_THRESHOLD, String.valueOf(customBuildData.get(MEDIUM_THRESHOLD)))
                        .replace(LOW_THRESHOLD, String.valueOf(customBuildData.get(LOW_THRESHOLD)));


                if (customBuildData.get(OSA_RESULTS_READY) != null) {
                    ret = ret
                            .replace(OSA_ENABLED, "true")
                            .replace(OSA_HIGH_RESULTS, String.valueOf(customBuildData.get(OSA_HIGH_RESULTS)))
                            .replace(OSA_MEDIUM_RESULTS, String.valueOf(customBuildData.get(OSA_MEDIUM_RESULTS)))
                            .replace(OSA_LOW_RESULTS, String.valueOf(customBuildData.get(OSA_LOW_RESULTS)))
                            .replace(OSA_THRESHOLD_ENABLED, String.valueOf(customBuildData.get(OSA_THRESHOLD_ENABLED)))
                            .replace(OSA_HIGH_THRESHOLD, String.valueOf(customBuildData.get(OSA_HIGH_THRESHOLD)))
                            .replace(OSA_MEDIUM_THRESHOLD, String.valueOf(customBuildData.get(OSA_MEDIUM_THRESHOLD)))
                            .replace(OSA_LOW_THRESHOLD, String.valueOf(customBuildData.get(OSA_LOW_THRESHOLD)))
                            .replace(OSA_VULNERABLE_LIBRARIES, String.valueOf(customBuildData.get(OSA_VULNERABLE_LIBRARIES)))
                            .replace(OSA_OK_LIBRARIES, String.valueOf(customBuildData.get(OSA_OK_LIBRARIES)));
                } else {
                    ret = ret
                            .replace(OSA_ENABLED, "false")
                            .replace(OSA_HIGH_RESULTS, "0")
                            .replace(OSA_MEDIUM_RESULTS, "0")
                            .replace(OSA_LOW_RESULTS, "0")
                            .replace(OSA_THRESHOLD_ENABLED, "false")
                            .replace(OSA_HIGH_THRESHOLD, "0")
                            .replace(OSA_MEDIUM_THRESHOLD, "0")
                            .replace(OSA_LOW_THRESHOLD, "0")
                            .replace(OSA_VULNERABLE_LIBRARIES, "0")
                            .replace(OSA_OK_LIBRARIES, "0");
                }
            }

        } else {
            return "";
        }

        return ret;
    }

    public void writeHtml(Writer writer, Map<String, Object> map) throws IOException {

    }


    private String getResultsTemplate() {
        String ret = null;
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/com/cx/plugin/resultsTemplate.html");
        if (resourceAsStream != null) {
            try {
                ret = IOUtils.toString(resourceAsStream, Charset.defaultCharset().name());
            } catch (IOException e) {
                log.warn("fail to get results template", e.getMessage());
            }
        }

        return ret;
    }
}
