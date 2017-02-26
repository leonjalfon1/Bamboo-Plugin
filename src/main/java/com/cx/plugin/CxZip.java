package com.cx.plugin;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import com.cx.plugin.dto.CxAbortException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * CxZip encapsulates the workspace folder zipping
 */

class CxZip {
    private long maxZipSizeInBytes = 209715200;
    private int numOfZippedFiles = 0;

    private String tempFileName;

    public CxZip(String tempFileName) {
        this.tempFileName = tempFileName;
    }

    File zipWorkspaceFolder(String baseDir, String filterPattern, final BuildLoggerAdapter buildLogger, boolean writeToLog)
            throws InterruptedException, IOException {
        if (baseDir == null || StringUtils.isEmpty(baseDir)) {
            throw new CxAbortException("Checkmarx Scan Failed: cannot acquire Bamboo workspace location. It can be due to workspace residing on a disconnected slave.");
        }
        buildLogger.info("Zipping workspace: '" + baseDir + "'");

        ZipListener zipListener;
        if (writeToLog) {
            zipListener = new ZipListener() {
                public void updateProgress(String fileName, long size) {
                    numOfZippedFiles++;
                    buildLogger.info("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
                }
            };
        } else {
            zipListener = new ZipListener() {
                public void updateProgress(String fileName, long size) {
                    numOfZippedFiles++;
                }
            };
        }
        File tempFile = File.createTempFile(tempFileName, ".bin");
        OutputStream fileOutputStream = new FileOutputStream(tempFile);

        File folder = new File(baseDir);
        try {
            new Zipper().zip(folder, filterPattern, fileOutputStream, maxZipSizeInBytes, zipListener);
        } catch (Zipper.MaxZipSizeReached e) {
            tempFile.delete();
            throw new IOException("Reached maximum upload size limit of " + FileUtils.byteCountToDisplaySize(maxZipSizeInBytes));
        } catch (Zipper.NoFilesToZip e) {
            throw new IOException("No files to zip");
        }

        buildLogger.info("Zipping complete with " + numOfZippedFiles + " files, total compressed size: " +
                FileUtils.byteCountToDisplaySize(tempFile.length() / 8 * 6)); // We print here the size of compressed sources before encoding to base 64
        buildLogger.info("Temporary file with zipped sources was created at: '" + tempFile.getAbsolutePath() + "'");

        return tempFile;
    }

    CxZip setMaxZipSizeInBytes(long maxZipSizeInBytes) {
        this.maxZipSizeInBytes = maxZipSizeInBytes;
        return this;
    }

    public CxZip setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
        return this;
    }

    public String getTempFileName() {
        return tempFileName;
    }

}
