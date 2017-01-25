package com.cx.plugin;

import java.io.IOException;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.util.concurrent.NotNull;
import com.cx.plugin.dto.CxParam;
import org.apache.commons.lang3.StringUtils;


/**
 * CxFolderPattern generates the patterns used for zipping the workspace folder
 */

public class CxFolderPattern {
    public String generatePattern(final ConfigurationMap configurationMap, final BuildLogger buildLogger, CxParam folderExclusion, boolean useFilterPattern) throws IOException, InterruptedException {

        String cxExclude = null;
        if (folderExclusion != null){
            configurationMap.get(folderExclusion.value()); //TODO add the ENV expansion
        }
        String cxPattern = "";
        if (useFilterPattern){
            cxPattern = configurationMap.get(CxParam.FILTER_PATTERN.value()) + ",";               //TODO, ask Sigal
        }
        return cxPattern + processExcludeFolders(cxExclude, buildLogger);
    }

    @NotNull
    private String processExcludeFolders(final String excludeFolders, final BuildLogger buildLogger) {
        if (excludeFolders == null || excludeFolders.length() == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String[] patterns = StringUtils.split(excludeFolders, ",\n");
        for (String p : patterns) {
            p = p.trim();
            if (p.length() > 0) {
                result.append("!**/");
                result.append(p);
                result.append("/**/*, ");
            }
        }
        buildLogger.addBuildLogEntry("Exclude folders converted to: '" + result.toString() + "'");
        return result.toString();
    }
}
