package org.slf4j.impl;


import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;


public class LoggerFactory implements ILoggerFactory {

    private static BambooLoggerAdapter logger;

     public  LoggerFactory() {
        logger = new BambooLoggerAdapter("");
    }

    public Logger getLogger(String name) {
        return logger;
    }
}