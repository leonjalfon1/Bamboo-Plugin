package com.cx.plugin.utils;

import com.atlassian.bamboo.build.logger.BuildLogger;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Created by: dorg.
 * Date: 04/03/2018.
 */
public class CxAppender extends AppenderSkeleton {

    private BuildLogger buildProgressLogger;
    private Filter filter = new Filter() {
        @Override
        public int decide(LoggingEvent loggingEvent) {
            if (loggingEvent.getLocationInformation().getClassName().startsWith("org.whitesource")) {
                return 0;
            } else {
                return -1;
            }
        }
    };


    public CxAppender(BuildLogger buildProgressLogger, String name) {
        this.name = name;
        this.buildProgressLogger = buildProgressLogger;
        this.headFilter = filter;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        buildProgressLogger.addBuildLogEntry(loggingEvent.getMessage().toString());
        if (loggingEvent.getThrowableInformation() != null && loggingEvent.getThrowableInformation().getThrowable() != null) {
            buildProgressLogger.addErrorLogEntry(loggingEvent.getMessage().toString(), loggingEvent.getThrowableInformation().getThrowable());
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}



