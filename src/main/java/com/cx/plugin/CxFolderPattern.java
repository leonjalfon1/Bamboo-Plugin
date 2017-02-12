package com.cx.plugin;

import java.io.IOException;
import java.util.HashMap;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.util.concurrent.NotNull;
import com.cx.plugin.dto.CxParam;
import org.apache.commons.lang3.StringUtils;


/**
 * CxFolderPattern generates the patterns used for zipping the workspace folder
 */

public class CxFolderPattern {
    public String generatePattern(final HashMap<String, String> configurationMap, final BuildLogger buildLogger, String folderExclusion) throws IOException, InterruptedException {
        String cxExclude = configurationMap.get(folderExclusion); //TODO add the ENV expansion
        String cxPattern = configurationMap.get(CxParam.FILTER_PATTERN);
        return cxPattern + "," + processExcludeFolders(cxExclude, buildLogger);
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
