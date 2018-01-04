package com.cx.plugin.utils;

import com.atlassian.util.concurrent.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;


/**
 * CxFolderPattern generates the patterns used for zipping the workspace folder
 */

public class CxFolderPattern {
    public String generatePattern(String folderExclusions, String filterPattern, CxBuildLoggerAdapter buildLogger) throws IOException, InterruptedException {

        String excludeFoldersPattern = processExcludeFolders(folderExclusions, buildLogger);

        if (StringUtils.isEmpty(filterPattern) && StringUtils.isEmpty(excludeFoldersPattern)) {
            return "";
        } else if (!StringUtils.isEmpty(filterPattern) && StringUtils.isEmpty(excludeFoldersPattern)) {
            return filterPattern;
        } else if (StringUtils.isEmpty(filterPattern) && !StringUtils.isEmpty(excludeFoldersPattern)) {
            return excludeFoldersPattern;
        } else {
            return filterPattern + "," + excludeFoldersPattern;
        }
    }


    @NotNull
    private String processExcludeFolders(String folderExclusions, CxBuildLoggerAdapter buildLogger) {
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
        buildLogger.info("Exclude folders converted to: '" + result.toString() + "'");
        return result.toString();
    }
}
