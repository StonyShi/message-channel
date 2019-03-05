package com.stony.mc.future;

import com.stony.mc.protocol.ExchangeProtocol;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.*;

/**
 * <p>mc-core
 * <p>com.stony.mc.store
 *
 * @author stony
 * @version 下午5:07
 * @since 2019/1/4
 */
public class ResultFuture extends FutureTask<ExchangeProtocol> {

    private volatile boolean success = false;
    private final ExchangeProtocol request;
    private final Long id;
    private volatile int getCount = 0;
    private ChannelHandlerContext context;
    static final Callable EMPTY_CALL = () -> null;

    public ResultFuture(ExchangeProtocol msg) {
        this(EMPTY_CALL, msg);
    }
    @SuppressWarnings("unchecked")
    public ResultFuture(Callable callable, ExchangeProtocol msg) {
        super(callable);
        this.request = msg;
        this.id = msg.getId();
    }

    @Override
    public void set(ExchangeProtocol v) {
        this.success = (v != null);
        super.set(v);
    }

    @Override
    public ExchangeProtocol get() throws InterruptedException, ExecutionException {
        ExchangeProtocol v = super.get();
        getCount++;
        return v;
    }

    @Override
    public ExchangeProtocol get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        ExchangeProtocol v = super.get(timeout, unit);
        getCount++;
        return v;
    }

    public Long getId() {
        return id;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public static ResultFuture wrap(ExchangeProtocol protocol) {
        return new ResultFuture(protocol);
    }
    public ExchangeProtocol getRequest() {
        return request;
    }

    public boolean isSuccess() {
        return success;
    }
}