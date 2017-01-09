package com.cx.plugin.dto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class Zipper {
    private static final Logger LOGGER = Logger.getLogger(Zipper.class);

    public Zipper() {
    }

    public void zip(File baseDir, String filterPatterns, OutputStream outputStream, long maxZipSize, ZipListener listener) throws IOException {
        assert baseDir != null : "baseDir must not be null";

        assert outputStream != null : "outputStream must not be null";

        DirectoryScanner ds = this.createDirectoryScanner(baseDir, filterPatterns);
        ds.setFollowSymlinks(true);
        ds.scan();
        this.printDebug(ds);
        if(ds.getIncludedFiles().length == 0) {
            outputStream.close();
            LOGGER.info("No files to zip");
            throw new Zipper.NoFilesToZip();
        } else {
            this.zipFile(baseDir, ds.getIncludedFiles(), outputStream, maxZipSize, listener);
        }
    }

    public void zip(File baseDir, String[] filterExcludePatterns, String[] filterIncludePatterns, OutputStream outputStream, long maxZipSize, ZipListener listener) throws IOException {
        assert baseDir != null : "baseDir must not be null";

        assert outputStream != null : "outputStream must not be null";

        DirectoryScanner ds = this.createDirectoryScanner(baseDir, filterExcludePatterns, filterIncludePatterns);
        ds.setFollowSymlinks(true);
        ds.scan();
        this.printDebug(ds);
        if(ds.getIncludedFiles().length == 0) {
            outputStream.close();
            LOGGER.info("No files to zip");
            throw new Zipper.NoFilesToZip();
        } else {
            this.zipFile(baseDir, ds.getIncludedFiles(), outputStream, maxZipSize, listener);
        }
    }

    public byte[] zip(File baseDir, String filterPatterns, long maxZipSize, ZipListener listener) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        this.zip(baseDir, (String)filterPatterns, (OutputStream)byteOutputStream, maxZipSize, listener);
        return byteOutputStream.toByteArray();
    }

    public byte[] zip(File baseDir, String[] filterExcludePatterns, String[] filterIncludePatterns, long maxZipSize, ZipListener listener) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        this.zip(baseDir, filterExcludePatterns, filterIncludePatterns, byteOutputStream, maxZipSize, listener);
        return byteOutputStream.toByteArray();
    }

    private void zipFile(File baseDir, String[] files, OutputStream outputStream, long maxZipSize, ZipListener listener) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setEncoding("UTF8");
        long compressedSize = 0L;
        double AVERAGE_ZIP_COMPRESSION_RATIO = 4.0D;
        String[] arr$ = files;
        int len$ = files.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String fileName = arr$[i$];
            LOGGER.debug("Adding file to zip: " + fileName);
            File file = new File(baseDir, fileName);
            if(!file.canRead()) {
                LOGGER.warn("Skipping unreadable file: " + file);
            } else {
                if(maxZipSize > 0L && (double)compressedSize + (double)file.length() / 4.0D > (double)maxZipSize) {
                    LOGGER.info("Maximum zip file size reached. Zip size: " + compressedSize + " bytes Limit: " + maxZipSize + " bytes");
                    zipOutputStream.close();
                    throw new Zipper.MaxZipSizeReached(compressedSize, maxZipSize);
                }

                if(listener != null) {
                    listener.updateProgress(fileName, compressedSize);
                }

                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);
                FileInputStream fileInputStream = new FileInputStream(file);
                IOUtils.copy(fileInputStream, zipOutputStream);
                fileInputStream.close();
                zipOutputStream.closeEntry();
                compressedSize += zipEntry.getCompressedSize();
            }
        }

        zipOutputStream.close();
    }

    private DirectoryScanner createDirectoryScanner(File baseDir, String filterPatterns) {
        LinkedList includePatterns = new LinkedList();
        LinkedList excludePatterns = new LinkedList();
        String[] patterns;
        if(filterPatterns != null) {
            patterns = StringUtils.split(filterPatterns, ",\n");
        } else {
            patterns = new String[0];
        }

        String[] arr$ = patterns;
        int len$ = patterns.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String pattern = arr$[i$];
            pattern = pattern.trim();
            if(pattern.length() > 0) {
                if(pattern.startsWith("!")) {
                    pattern = pattern.substring(1);
                    excludePatterns.add(pattern);
                    LOGGER.debug("Exclude pattern detected: >" + pattern + "<");
                } else {
                    includePatterns.add(pattern);
                    LOGGER.debug("Include pattern detected: >" + pattern + "<");
                }
            }
        }

        return this.createDirectoryScanner(baseDir, (String[])excludePatterns.toArray(new String[0]), (String[])includePatterns.toArray(new String[0]));
    }

    private DirectoryScanner createDirectoryScanner(File baseDir, String[] filterExcludePatterns, String[] filterIncludePatterns) {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(baseDir);
        ds.setCaseSensitive(false);
        ds.setFollowSymlinks(false);
        ds.setErrorOnMissingDir(false);
        if(filterIncludePatterns != null && filterIncludePatterns.length > 0) {
            ds.setIncludes(filterIncludePatterns);
        }

        if(filterExcludePatterns != null && filterExcludePatterns.length > 0) {
            ds.setExcludes(filterExcludePatterns);
        }

        return ds;
    }

    private void printDebug(DirectoryScanner ds) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Base Directory: " + ds.getBasedir());
            String[] arr$ = ds.getIncludedFiles();
            int len$ = arr$.length;

            int i$;
            String file;
            for(i$ = 0; i$ < len$; ++i$) {
                file = arr$[i$];
                LOGGER.debug("Included: " + file);
            }

            arr$ = ds.getExcludedFiles();
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
                file = arr$[i$];
                LOGGER.debug("Excluded File: " + file);
            }

            arr$ = ds.getExcludedDirectories();
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
                file = arr$[i$];
                LOGGER.debug("Excluded Dir: " + file);
            }

            arr$ = ds.getNotFollowedSymlinks();
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
                file = arr$[i$];
                LOGGER.debug("Not followed symbolic link: " + file);
            }

        }
    }

    public static class NoFilesToZip extends IOException {
        public NoFilesToZip() {
            super("No files to zip");
        }
    }

    public static class MaxZipSizeReached extends IOException {
        private long compressedSize;
        private long maxZipSize;

        public MaxZipSizeReached(long compressedSize, long maxZipSize) {
            super("Zip compressed size reached a limit of " + maxZipSize + " bytes");
        }

        public long getCompressedSize() {
            return this.compressedSize;
        }

        public long getMaxZipSize() {
            return this.maxZipSize;
        }
    }
}
