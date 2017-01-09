package org.slf4j.impl;


import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.NullBuildLogger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * Created by: Dorg.
 * Date: 14/09/2016.
 */
public class BambooLoggerAdapter extends MarkerIgnoringBase {

    private static BuildLogger log = new NullBuildLogger();


    public static void setLogger(BuildLogger log) {
        BambooLoggerAdapter.log = log;
    }

    public BambooLoggerAdapter(String name) {
        this.name = name;
    }


    public boolean isTraceEnabled() {
        return false;
    }

    public void trace(String s) {

    }

    public void trace(String s, Object o) {
    }

    public void trace(String s, Object o, Object o1) {
    }

    public void trace(String s, Object... objects) {
    }

    public void trace(String s, Throwable throwable) {
    }

    /********************************************************/    //TODO no DEBUG what wil happend DOR
    public boolean isDebugEnabled() {
        return false;  //TODO  ??????
    }

    public void debug(String s) {

    }

    public void debug(String s, Object o) {
    }

    public void debug(String s, Object o, Object o1) {

    }

    public void debug(String s, Object... objects) {
        ;
    }

    public void debug(String s, Throwable throwable) {
    }

    /****************************************************************/


    public boolean isInfoEnabled() {
        return log != null;   //TODO  ??????
    }

    public void info(String s) {
        log.addBuildLogEntry(s);
    }

    public void info(String s, Object o) {
        FormattingTuple ft = MessageFormatter.format(s, o);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void info(String s, Object o, Object o1) {
        FormattingTuple ft = MessageFormatter.format(s, o, o1);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void info(String s, Object... objects) {
        FormattingTuple ft = MessageFormatter.format(s, objects);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void info(String s, Throwable throwable) {
        log.addErrorLogEntry(s, throwable);
    }


    public boolean isWarnEnabled() {
        return log != null;
    } //TODO

    public void warn(String s) {
        log.addErrorLogEntry(s);
    }

    public void warn(String s, Object o) {
        FormattingTuple ft = MessageFormatter.format(s, o);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void warn(String s, Object... objects) {
        FormattingTuple ft = MessageFormatter.format(s, objects);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void warn(String s, Object o, Object o1) {
        FormattingTuple ft = MessageFormatter.format(s, o, o1);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void warn(String s, Throwable throwable) {
        log.addErrorLogEntry(s, throwable);
    }

    public boolean isErrorEnabled() {
        return log != null;
    }   //TODO  ??????

    public void error(String s) {
        log.addErrorLogEntry(s);
    }

    public void error(String s, Object o) {
        FormattingTuple ft = MessageFormatter.format(s, o);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void error(String s, Object o, Object o1) {
        FormattingTuple ft = MessageFormatter.format(s, o, o1);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void error(String s, Object... objects) {
        FormattingTuple ft = MessageFormatter.format(s, objects);
        log.addErrorLogEntry(ft.getMessage(), ft.getThrowable());
    }

    public void error(String s, Throwable throwable) {
        log.addErrorLogEntry(s, throwable);
    }
}
