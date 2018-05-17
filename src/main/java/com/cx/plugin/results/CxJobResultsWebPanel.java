package com.cx.plugin.results;

import com.atlassian.bamboo.resultsummary.BuildResultsSummaryImpl;
import com.atlassian.plugin.web.model.WebPanel;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import static com.cx.plugin.utils.CxParam.HTML_REPORT;

/**
 * Created by: dorg.
 * Date: 01/02/2017.
 */

public class CxJobResultsWebPanel implements WebPanel {


    public String getHtml(Map<String, Object> map) {
        BuildResultsSummaryImpl a = (BuildResultsSummaryImpl) map.get("resultSummary");
        Map<String, String> results = a.getCustomBuildData();

        return results.get(HTML_REPORT);
    }

    public void writeHtml(Writer writer, Map<String, Object> map) throws IOException {

    }


}
