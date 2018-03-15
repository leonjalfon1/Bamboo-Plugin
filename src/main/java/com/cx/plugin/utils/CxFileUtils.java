package com.cx.plugin.utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Galn on 28/02/2017.
 */
public abstract class CxFileUtils {

    public static void deleteTempFiles(CxLoggerAdapter buildLoggerAdapter, String zipFileName) {

        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            deleteFile(tempDir, zipFileName);
        } catch (Exception e) {
            buildLoggerAdapter.error("Failed to delete temp files: " + e.getMessage());
        }

    }

    private static void deleteFile(String folder, String prefix) {

        GenericPrefixFilter filter = new GenericPrefixFilter(prefix);
        File dir = new File(folder);

        //list out all the file name with prefix
        String[] list = dir.list(filter);

        if (list == null || list.length == 0) return;

        File fileDelete;

        for (String file : list) {
            String temp = folder + File.separator + file;
            fileDelete = new File(temp);
            fileDelete.delete();
        }
    }

    //inner class, generic prefix filter
    private static class GenericPrefixFilter implements FilenameFilter {

        private String prefix;

        private GenericPrefixFilter(String prefix) {
            this.prefix = prefix;
        }

        public boolean accept(File dir, String name) {
            return (name.startsWith(prefix));
        }
    }
}
