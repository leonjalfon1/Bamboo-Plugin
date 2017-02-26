package com.cx.plugin;

import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.plugin.web.model.WebPanel;
import com.cx.plugin.dto.ResultUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Created by: dorg.
 * Date: 01/02/2017.
 */

public class CxPlanResultsWebPanel implements WebPanel {

    public String getHtml(Map<String, Object> map) {

        ChainResultsSummaryImpl a = (ChainResultsSummaryImpl) map.get("resultSummary");
        Map<String, String> costumeBuildData = a.getOrderedJobResultSummaries().get(0).getCustomBuildData();
        String ret =  ResultUtils.resolveCostumeBuildData(costumeBuildData);
        return ret;


    }

    public void writeHtml(Writer writer, Map<String, Object> map) throws IOException {

    }


}
