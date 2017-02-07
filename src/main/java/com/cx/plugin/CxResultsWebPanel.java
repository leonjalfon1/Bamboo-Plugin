package com.cx.plugin;

import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.plugin.web.model.WebPanel;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by: dorg.
 * Date: 01/02/2017.
 */

public class CxResultsWebPanel implements WebPanel{

    private static final Logger log = LoggerFactory.getLogger(CxResultsWebPanel.class);

    public String getHtml(Map<String, Object> map) {

        ChainResultsSummaryImpl a = (ChainResultsSummaryImpl)map.get("resultSummary");

        Map<String, String> customBuildData = a.getOrderedJobResultSummaries().get(0).getCustomBuildData();

        String ret = "";
        if(customBuildData.get(CxResultsConst.SAST_RESULTS_READY) != null) {
            String resultsTemplate = getResultsTemplate();
            if(resultsTemplate != null) {

                ret = resultsTemplate
                        .replaceAll(CxResultsConst.HIGH_RESULTS, String.valueOf(customBuildData.get(CxResultsConst.HIGH_RESULTS)))
                        .replace(CxResultsConst.MEDIUM_RESULTS, String.valueOf(customBuildData.get(CxResultsConst.MEDIUM_RESULTS)))
                        .replace(CxResultsConst.LOW_RESULTS, String.valueOf(customBuildData.get(CxResultsConst.LOW_RESULTS)))
                        .replace(CxResultsConst.THRESHOLD_ENABLED, String.valueOf(customBuildData.get(CxResultsConst.THRESHOLD_ENABLED)))
                        .replace(CxResultsConst.HIGH_THRESHOLD, String.valueOf(customBuildData.get(CxResultsConst.HIGH_THRESHOLD)))
                        .replace(CxResultsConst.MEDIUM_THRESHOLD, String.valueOf(customBuildData.get(CxResultsConst.MEDIUM_THRESHOLD)))
                        .replace(CxResultsConst.LOW_THRESHOLD, String.valueOf(customBuildData.get(CxResultsConst.LOW_THRESHOLD)));


                if(customBuildData.get(CxResultsConst.OSA_RESULTS_READY) != null) {
                    ret = ret
                            .replace(CxResultsConst.OSA_ENABLED, "true")
                            .replace(CxResultsConst.OSA_HIGH_RESULTS, String.valueOf(customBuildData.get(CxResultsConst.OSA_HIGH_RESULTS)))
                            .replace(CxResultsConst.OSA_MEDIUM_RESULTS, String.valueOf(customBuildData.get(CxResultsConst.OSA_MEDIUM_RESULTS)))
                            .replace(CxResultsConst.OSA_LOW_RESULTS, String.valueOf(customBuildData.get(CxResultsConst.OSA_LOW_RESULTS)))
                            .replace(CxResultsConst.OSA_THRESHOLD_ENABLED, String.valueOf(customBuildData.get(CxResultsConst.OSA_THRESHOLD_ENABLED)))
                            .replace(CxResultsConst.OSA_HIGH_THRESHOLD, String.valueOf(customBuildData.get(CxResultsConst.OSA_HIGH_THRESHOLD)))
                            .replace(CxResultsConst.OSA_MEDIUM_THRESHOLD, String.valueOf(customBuildData.get(CxResultsConst.OSA_MEDIUM_THRESHOLD)))
                            .replace(CxResultsConst.OSA_LOW_THRESHOLD, String.valueOf(customBuildData.get(CxResultsConst.OSA_LOW_THRESHOLD)))
                            .replace(CxResultsConst.OSA_VULNERABLE_LIBRARIES, String.valueOf(customBuildData.get(CxResultsConst.OSA_VULNERABLE_LIBRARIES)))
                            .replace(CxResultsConst.OSA_OK_LIBRARIES, String.valueOf(customBuildData.get(CxResultsConst.OSA_OK_LIBRARIES)));
                } else {
                    ret = ret
                            .replace(CxResultsConst.OSA_ENABLED, "false")
                            .replace(CxResultsConst.OSA_HIGH_RESULTS, "0")
                            .replace(CxResultsConst.OSA_MEDIUM_RESULTS, "0")
                            .replace(CxResultsConst.OSA_LOW_RESULTS, "0")
                            .replace(CxResultsConst.OSA_THRESHOLD_ENABLED, "false")
                            .replace(CxResultsConst.OSA_HIGH_THRESHOLD, "0")
                            .replace(CxResultsConst.OSA_MEDIUM_THRESHOLD, "0")
                            .replace(CxResultsConst.OSA_LOW_THRESHOLD, "0")
                            .replace(CxResultsConst.OSA_VULNERABLE_LIBRARIES, "0")
                            .replace(CxResultsConst.OSA_OK_LIBRARIES, "0");
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
        if(resourceAsStream != null) {
            try {
                ret = IOUtils.toString(resourceAsStream, Charset.defaultCharset().name());
            } catch (IOException e) {
                log.warn("fail to get results template", e.getMessage());
            }
        }

        return ret;
    }
}
