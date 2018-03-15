package com.cx.plugin.utils;

import com.atlassian.bamboo.task.TaskException;
import com.atlassian.util.concurrent.NotNull;
import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import com.cx.plugin.dto.CxAbortException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

import static com.cx.plugin.utils.CxParam.TEMP_FILE_NAME_TO_ZIP;


/**
 * CxZipUtils generates the patterns used for zipping the workspace folder
 */

public class CxZipUtils {
    private int numOfZippedFiles = 0;

    public File zipWorkspaceFolder(String baseDir, String folderExclusions, String filterPattern, long maxZipBytes, boolean writeToLog, final CxLoggerAdapter log) throws IOException, InterruptedException {
        final String combinedFilterPattern = generatePattern(folderExclusions, filterPattern, log);
        if (baseDir == null || StringUtils.isEmpty(baseDir)) {
            throw new CxAbortException("Checkmarx Scan Failed: cannot acquire Bamboo workspace location. It can be due to workspace residing on a disconnected slave.");
        }
        log.info("Zipping workspace: '" + baseDir + "'");
        ZipListener zipListener;
        if (writeToLog) {
            zipListener = new ZipListener() {
                public void updateProgress(String fileName, long size) {
                    numOfZippedFiles++;
                    log.info("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
                }
            };
        } else {
            zipListener = new ZipListener() {
                public void updateProgress(String fileName, long size) {
                    numOfZippedFiles++;
                }
            };
        }
        File tempFile = File.createTempFile(TEMP_FILE_NAME_TO_ZIP, ".bin");
        OutputStream fileOutputStream = new FileOutputStream(tempFile);

        File folder = new File(baseDir);
        try {
            new Zipper().zip(folder, combinedFilterPattern, fileOutputStream, maxZipBytes, zipListener);
        } catch (Zipper.MaxZipSizeReached e) {
            tempFile.delete();
            throw new IOException("Reached maximum upload size limit of " + FileUtils.byteCountToDisplaySize(maxZipBytes));
        } catch (Zipper.NoFilesToZip e) {
            throw new IOException("No files to zip");
        }

        log.info("Zipping complete with " + numOfZippedFiles + " files, total compressed size: " +
                FileUtils.byteCountToDisplaySize(tempFile.length()));
        log.info("Temporary file with zipped sources was created at: '" + tempFile.getAbsolutePath() + "'");

        return tempFile;
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

    private String generatePattern(String folderExclusions, String filterPattern, CxLoggerAdapter buildLogger) throws IOException, InterruptedException {

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
