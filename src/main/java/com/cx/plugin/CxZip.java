package com.cx.plugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;

import com.cx.plugin.dto.CxAbortException;
import com.cx.plugin.dto.ZipListener;
import com.cx.plugin.dto.Zipper;
import org.apache.commons.io.FileUtils;

import com.atlassian.bamboo.build.logger.BuildLogger;

import org.apache.commons.lang.StringUtils;


/**
 * CxZip encapsulates the workspace folder zipping
 */

public class CxZip {
    private static final long MAXZIPSIZEBYTES = 209715200;
    private int numOfZippedFiles = 0;

    public File ZipWorkspaceFolder(final String baseDir, final String filterPattern, final BuildLogger buildLogger)
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

        final File tempFile = File.createTempFile("zippedSource", ".bin");
        final OutputStream fileOutputStream = new FileOutputStream(tempFile);

        File folder = new File(baseDir);
        try {
            new Zipper().zip(folder, filterPattern, fileOutputStream, MAXZIPSIZEBYTES, zipListener);
        } catch (Zipper.MaxZipSizeReached e) {
            throw new IOException("Reached maximum upload size limit of " + FileUtils.byteCountToDisplaySize(MAXZIPSIZEBYTES));
        } catch (Zipper.NoFilesToZip e) {
            throw new IOException("No files to zip");
        }

        buildLogger.addBuildLogEntry("Zipping complete with " + numOfZippedFiles + " files, total compressed size: " +
                FileUtils.byteCountToDisplaySize(tempFile.length() / 8 * 6)); // We print here the size of compressed sources before encoding to base 64
        buildLogger.addBuildLogEntry("Temporary file with zipped sources was created at: '" + tempFile.getAbsolutePath() + "'");

        return tempFile;
    }

    public File zipSourceCode(final String baseDir, String filterPattern) throws IOException, InterruptedException {
        //    ZipperCallable zipperCallable = new ZipperCallable(filterPattern); //TODO understand the role of the callable
        final File tempFile = File.createTempFile("zippedSource", ".bin");
        final OutputStream fileOutputStream = new FileOutputStream(tempFile);

        File folder = new File(baseDir);
        try {
            new Zipper().zip(folder, filterPattern, fileOutputStream, MAXZIPSIZEBYTES, null);
        } catch (Zipper.MaxZipSizeReached e) {
            throw new IOException("Reached maximum upload size limit of " + FileUtils.byteCountToDisplaySize(MAXZIPSIZEBYTES));
        } catch (Zipper.NoFilesToZip e) {
            throw new IOException("No files to zip");
        }

        return tempFile;
    }


}
