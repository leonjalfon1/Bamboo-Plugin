package com.cx.plugin.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Galn on 28/02/2017.
 */
public class FileChecker {//TODO - Dor - how make the calss static- ther should be one way
    public static final Logger log = LoggerFactory.getLogger(FileChecker.class);

    public static void deleteFile(String folder, String prefix){

        GenericPrefixFilter filter = new GenericPrefixFilter(prefix);
        File dir = new File(folder);

        //list out all the file name with .txt extension
        String[] list = dir.list(filter);

        if (list.length == 0) return;

        File fileDelete;

        for (String file : list){
            String temp = new StringBuffer(folder)
                    .append(File.separator)
                    .append(file).toString();
            fileDelete = new File(temp);
            boolean isDeleted = fileDelete.delete();
            log.warn("file : " + temp + " is deleted : " + isDeleted);
        }
    }

    //inner class, generic extension filter
    public static class GenericPrefixFilter implements FilenameFilter {

        private String prefix;

        public GenericPrefixFilter(String prefix) {
            this.prefix = prefix;
        }

        public boolean accept(File dir, String name) {
            return (name.startsWith(prefix));
        }
    }
}
