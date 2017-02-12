package com.cx.plugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;

import com.cx.plugin.dto.CxAbortException;
import org.apache.commons.io.FileUtils;

import com.atlassian.bamboo.build.logger.BuildLogger;

import org.apache.commons.lang.StringUtils;

import com.checkmarx.components.zipper.Zipper;
import com.checkmarx.components.zipper.ZipListener;
/**
 * CxZip encapsulates the workspace folder zipping
 */

public class CxZip {
    private long maxZipSizeInBytes = 209715200;
    private int numOfZippedFiles = 0;
    private String tempFileName = "CxZippedSource";


    public File zipWorkspaceFolder(String baseDir, String filterPattern, final BuildLogger buildLogger)
            throws InterruptedException, IOException {
        if (baseDir == null || StringUtils.isEmpty(baseDir)) {
            throw new CxAbortException("Checkmarx Scan Failed: cannot acquire Bamboo workspace location. It can be due to workspace residing on a disconnected slave.");
        }
        buildLogger.addBuildLogEntry("Zipping workspace: '" + baseDir + "'");

        ZipListener zipListener = new ZipListener() {
            public void updateProgress(String fileName, long size) {
                numOfZippedFiles++;
                buildLogger.addBuildLogEntry("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
            }
        };

        File tempFile = File.createTempFile(tempFileName, ".bin");
        OutputStream fileOutputStream = new FileOutputStream(tempFile);

        File folder = new File(baseDir);
        try {
            new Zipper().zip(folder, filterPattern, fileOutputStream, maxZipSizeInBytes, zipListener);
        } catch (Zipper.MaxZipSizeReached e) {
            throw new IOException("Reached maximum upload size limit of " + FileUtils.byteCountToDisplaySize(maxZipSizeInBytes));
        } catch (Zipper.NoFilesToZip e) {
            throw new IOException("No files to zip");
        }

        buildLogger.addBuildLogEntry("Zipping complete with " + numOfZippedFiles + " files, total compressed size: " +
                FileUtils.byteCountToDisplaySize(tempFile.length() / 8 * 6)); // We print here the size of compressed sources before encoding to base 64
        buildLogger.addBuildLogEntry("Temporary file with zipped sources was created at: '" + tempFile.getAbsolutePath() + "'");

        return tempFile;
    }

    public CxZip setMaxZipSizeInBytes(long maxZipSizeInBytes) {
        this.maxZipSizeInBytes = maxZipSizeInBytes;
        return this;
    }

    public CxZip setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
        return this;
    }

}
