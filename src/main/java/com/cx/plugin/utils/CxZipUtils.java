package com.cx.plugin.utils;

import com.atlassian.bamboo.task.TaskException;
import com.atlassian.util.concurrent.NotNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.cx.plugin.dto.CxParam.TEMP_FILE_NAME_TO_ZIP;


/**
 * CxZipUtils generates the patterns used for zipping the workspace folder
 */

public class CxZipUtils {

    public File zipWorkspaceFolder(String zipDir, String folderExclusions, String filterPattern, long maxZipBytes, boolean writeLog, CxLoggerAdapter log) throws IOException, InterruptedException {
        final String combinedFilterPattern = generatePattern(folderExclusions, filterPattern, log);
        CxZip cxZip = new CxZip(TEMP_FILE_NAME_TO_ZIP).setMaxZipSizeInBytes(maxZipBytes);
        return cxZip.zipWorkspaceFolder(zipDir, combinedFilterPattern, log, writeLog);
    }

    public static byte[] getBytesFromZippedSources(File zipTempFile, CxLoggerAdapter log) throws TaskException {
        log.info("Converting zipped sources to byte array");
        byte[] zipFileByte;
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(zipTempFile);
            zipFileByte = IOUtils.toByteArray(fileStream);
        } catch (Exception e) {
            throw new TaskException("Fail to set zipped file into project: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fileStream);
        }
        return zipFileByte;
    }

    public String generatePattern(String folderExclusions, String filterPattern, CxLoggerAdapter buildLogger) throws IOException, InterruptedException {

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
    private String processExcludeFolders(String folderExclusions, CxLoggerAdapter buildLogger) {
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
                result.append("/**, ");
            }
        }
        buildLogger.info("Exclude folders converted to: '" + result.toString() + "'");
        return result.toString();
    }
}
