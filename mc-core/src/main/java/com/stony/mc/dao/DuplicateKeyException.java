package com.stony.mc.dao;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * <p>message-channel
 * <p>com.stony.mc.dao
 *
 * @author stony
 * @version 上午9:40
 * @since 2019/4/12
 */
public class DuplicateKeyException extends SQLException {
    final SQLException delegate;

    public DuplicateKeyException(SQLException delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getMessage() {
        return delegate.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return delegate.getLocalizedMessage();
    }

    @Override
    public void printStackTrace() {
        delegate.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        delegate.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        delegate.printStackTrace(s);
    }

    public Throwable getOriginalCause() {
        return this.delegate;
    }
}