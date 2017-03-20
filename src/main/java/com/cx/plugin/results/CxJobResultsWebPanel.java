package com.cx.plugin.results;

import com.atlassian.bamboo.resultsummary.BuildResultsSummaryImpl;
import com.atlassian.plugin.web.model.WebPanel;
import com.cx.plugin.utils.CxResultUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Created by: dorg.
 * Date: 01/02/2017.
 */

public class CxJobResultsWebPanel implements WebPanel {


    public String getHtml(Map<String, Object> map) {

        BuildResultsSummaryImpl a = (BuildResultsSummaryImpl) map.get("resultSummary");
        Map<String, String> customBuildData = a.getCustomBuildData();
        String ret = CxResultUtils.resolveCostumeBuildData(customBuildData);
        return ret;
    }

    public void writeHtml(Writer writer, Map<String, Object> map) throws IOException {

    }


}
