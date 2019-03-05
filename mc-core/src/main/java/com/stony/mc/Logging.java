package com.stony.mc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午2:47
 * @since 2019/1/22
 */
public class Logging {
    String loggerName = this.getClass().getName();
    protected final Logger logger = LoggerFactory.getLogger(loggerName);


    public String getName() {
        return loggerName;
    }


    public void debug(org.slf4j.Marker marker, java.lang.String format, java.lang.Object... argArray) {
        if (isDebugEnabled()) {
            logger.debug(marker, format, argArray);
        }
    }


    public void debug(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2, java.lang.Object arg3) {
        if (isDebugEnabled()) {
            logger.debug(marker, format, arg2, arg3);
        }
    }


    public void debug(java.lang.String format) {
        if (isDebugEnabled()) {
            logger.debug(format);
        }
    }


    public void debug(org.slf4j.Marker marker, java.lang.String format, java.lang.Throwable arg2) {
        if (isDebugEnabled()) {
            logger.debug(marker, format, arg2);
        }
    }


    public void debug(org.slf4j.Marker marker, java.lang.String format) {
        if (isDebugEnabled()) {
            logger.debug(marker, format);
        }
    }


    public void debug(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2) {
        if (isDebugEnabled()) {
            logger.debug(marker, format, arg2);
        }
    }


    public void debug(java.lang.String format, java.lang.Throwable arg1) {
        if (isDebugEnabled()) {
            logger.debug(format, arg1);
        }
    }


    public void debug(java.lang.String format, Object... argArray) {
        if (isDebugEnabled()) {
            logger.debug(format, argArray);
        }
    }


    public void debug(java.lang.String format, java.lang.Object arg1, java.lang.Object arg2) {
        if (isDebugEnabled()) {
            logger.debug(format, arg1, arg2);
        }
    }


    public void debug(java.lang.String format, java.lang.Object arg1) {
        if (isDebugEnabled()) {
            logger.debug(format, arg1);
        }
    }


    public void error(org.slf4j.Marker marker, java.lang.String format) {
        logger.error(marker, format);
    }


    public void error(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2) {
        logger.error(marker, format, arg2);
    }


    public void error(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2, java.lang.Object arg3) {
        logger.error(marker, format, arg2, arg3);
    }


    public void error(org.slf4j.Marker marker, java.lang.String format, java.lang.Object... argArray) {
        logger.error(marker, format, argArray);
    }


    public void error(org.slf4j.Marker marker, java.lang.String format, java.lang.Throwable arg2) {
        logger.error(marker, format, arg2);
    }


    public void error(java.lang.String format) {
        logger.error(format);
    }


    public void error(java.lang.String format, java.lang.Object arg1) {
        logger.error(format, arg1);
    }


    public void error(java.lang.String format, java.lang.Object arg1, java.lang.Object arg2) {
        logger.error(format, arg1, arg2);
    }


    public void error(java.lang.String format, Object... argArray) {
        logger.error(format, argArray);
    }


    public void error(java.lang.String format, java.lang.Throwable arg1) {
        logger.error(format, arg1);
    }


    public void info(org.slf4j.Marker marker, java.lang.String format, java.lang.Object... argArray) {
        logger.info(marker, format, argArray);
    }


    public void info(org.slf4j.Marker marker, java.lang.String format) {
        logger.info(marker, format);
    }


    public void info(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2) {
        logger.info(marker, format, arg2);
    }


    public void info(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2, java.lang.Object arg3) {
        logger.info(marker, format, arg2, arg3);
    }


    public void info(java.lang.String format, java.lang.Throwable arg1) {
        logger.info(format, arg1);
    }


    public void info(org.slf4j.Marker marker, java.lang.String format, java.lang.Throwable arg2) {
        logger.info(marker, format, arg2);
    }


    public void info(java.lang.String format, java.lang.Object arg1) {
        logger.info(format, arg1);
    }


    public void info(java.lang.String format, java.lang.Object arg1, java.lang.Object arg2) {
        logger.info(format, arg1, arg2);
    }


    public void info(java.lang.String format, Object... argArray) {
        logger.info(format, argArray);
    }


    public void info(java.lang.String format) {
        logger.info(format);
    }


    public boolean isDebugEnabled(org.slf4j.Marker marker) {
        return logger.isDebugEnabled(marker);
    }


    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }


    public boolean isErrorEnabled(org.slf4j.Marker marker) {
        return logger.isErrorEnabled(marker);
    }


    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }


    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }


    public boolean isInfoEnabled(org.slf4j.Marker marker) {
        return logger.isInfoEnabled(marker);
    }


    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }


    public boolean isTraceEnabled(org.slf4j.Marker marker) {
        return logger.isTraceEnabled(marker);
    }


    public boolean isWarnEnabled(org.slf4j.Marker marker) {
        return logger.isWarnEnabled(marker);
    }


    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }


    public void trace(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2, java.lang.Object arg3) {
        if (isTraceEnabled()) {
            logger.trace(marker, format, arg2, arg3);
        }
    }


    public void trace(java.lang.String format, java.lang.Object arg1, java.lang.Object arg2) {
        if (isTraceEnabled()) {
            logger.trace(format, arg1, arg2);
        }
    }


    public void trace(java.lang.String format, java.lang.Object arg1) {
        if (isTraceEnabled()) {
            logger.trace(format, arg1);
        }
    }


    public void trace(java.lang.String format) {
        if (isTraceEnabled()) {
            logger.trace(format);
        }
    }


    public void trace(org.slf4j.Marker marker, java.lang.String format, java.lang.Object... argArray) {
        if (isTraceEnabled()) {
            logger.trace(marker, format, argArray);
        }
    }


    public void trace(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2) {
        if (isTraceEnabled()) {
            logger.trace(marker, format, arg2);
        }
    }


    public void trace(org.slf4j.Marker marker, java.lang.String format) {
        if (isTraceEnabled()) {
            logger.trace(marker, format);
        }
    }


    public void trace(org.slf4j.Marker marker, java.lang.String format, java.lang.Throwable arg2) {
        if (isTraceEnabled()) {
            logger.trace(marker, format, arg2);
        }
    }


    public void trace(java.lang.String format, java.lang.Throwable arg1) {
        if (isTraceEnabled()) {
            logger.trace(format, arg1);
        }
    }


    public void trace(java.lang.String format, Object... argArray) {
        if (isTraceEnabled()) {
            logger.trace(format, argArray);
        }
    }


    public void warn(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2) {
        logger.warn(marker, format, arg2);
    }


    public void warn(org.slf4j.Marker marker, java.lang.String format) {
        logger.warn(marker, format);
    }


    public void warn(java.lang.String format, java.lang.Throwable arg1) {
        logger.warn(format, arg1);
    }


    public void warn(java.lang.String format, java.lang.Object arg1, java.lang.Object arg2) {
        logger.warn(format, arg1, arg2);
    }


    public void warn(org.slf4j.Marker marker, java.lang.String format, java.lang.Throwable arg2) {
        logger.warn(marker, format, arg2);
    }


    public void warn(org.slf4j.Marker marker, java.lang.String format, java.lang.Object... argArray) {
        logger.warn(marker, format, argArray);
    }


    public void warn(java.lang.String format) {
        logger.warn(format);
    }

    public void warn(org.slf4j.Marker marker, java.lang.String format, java.lang.Object arg2, java.lang.Object arg3) {
        logger.warn(marker, format, arg2, arg3);
    }


    public void warn(java.lang.String format, Object... argArray) {
        logger.warn(format, argArray);
    }


    public void warn(java.lang.String format, java.lang.Object arg1) {
        logger.warn(format, arg1);
    }
}
