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
    public static String generatePattern(String folderExclusions, String filterPattern, BuildLogger buildLogger) throws IOException, InterruptedException {

        String excludeFoldersPattern = processExcludeFolders(folderExclusions, buildLogger);

        if(StringUtils.isEmpty(filterPattern) && StringUtils.isEmpty(excludeFoldersPattern)) {
            return "";
        } else if(!StringUtils.isEmpty(filterPattern) && StringUtils.isEmpty(excludeFoldersPattern)) {
            return filterPattern;
        } else if(StringUtils.isEmpty(filterPattern) && !StringUtils.isEmpty(excludeFoldersPattern)) {
            return excludeFoldersPattern;
        } else {
            return filterPattern + "," + excludeFoldersPattern;
        }
    }



    @NotNull
    private static String processExcludeFolders(String folderExclusions, BuildLogger buildLogger) {
        if (StringUtils.isEmpty(folderExclusions)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String[] patterns = StringUtils.split(folderExclusions, ",\n");
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
