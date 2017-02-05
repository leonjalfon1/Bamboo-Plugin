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

        //todo check the chain of gets
        Map<String, String> customBuildData = a.getOrderedJobResultSummaries().get(0).getCustomBuildData();

        String ret = "<div>WAWAWAWAWA</div>";
        String resultsTemplate = getResultsTemplate();
        if(resultsTemplate != null) {

            ret = resultsTemplate
                    .replace(CxResultsConst.HIGH_RESULTS, customBuildData.get(CxResultsConst.HIGH_RESULTS))
                    .replace(CxResultsConst.MEDIUM_RESULTS, customBuildData.get(CxResultsConst.MEDIUM_RESULTS))
                    .replace(CxResultsConst.LOW_RESULTS, customBuildData.get(CxResultsConst.LOW_RESULTS))
                    .replace(CxResultsConst.THRESHOLD_ENABLED, customBuildData.get(CxResultsConst.THRESHOLD_ENABLED))
                    .replace(CxResultsConst.HIGH_THRESHOLD, customBuildData.get(CxResultsConst.HIGH_THRESHOLD) == null ? "null" : customBuildData.get(CxResultsConst.HIGH_THRESHOLD))
                    .replace(CxResultsConst.MEDIUM_THRESHOLD, customBuildData.get(CxResultsConst.MEDIUM_THRESHOLD) == null ? "null" : customBuildData.get(CxResultsConst.MEDIUM_THRESHOLD))
                    .replace(CxResultsConst.LOW_THRESHOLD, customBuildData.get(CxResultsConst.LOW_THRESHOLD) == null ? "null" : customBuildData.get(CxResultsConst.LOW_THRESHOLD));

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
