package com.stony.mc.future;

import com.stony.mc.listener.ResultFutureListener;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeStatus;

/**
 * <p>mc-core
 * <p>com.stony.mc.store
 *
 * @author stony
 * @version 下午2:21
 * @since 2019/1/10
 */
public class ResultFutureListenable extends ResultFuture {

    private ResultFutureListener futureListener;

    public ResultFutureListenable(ExchangeProtocol msg, ResultFutureListener futureListener) {
        super(msg);
        this.futureListener = futureListener;
    }

    public ResultFutureListenable(ExchangeProtocol msg) {
        super(msg);
    }
    public ResultFutureListenable listener(ResultFutureListener futureListener) {
        this.futureListener = futureListener;
        return this;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void set(ExchangeProtocol v) {
        super.set(v);
        if(futureListener != null) {
            if(v != null) {
                if(isDone() && ExchangeStatus.OK == v.getStatus()) {
                    futureListener.onSuccess(v);
                } else {
                    futureListener.onFailed(new RuntimeException(v.getStatus().toString()));
                }
            } else {
                futureListener.onFailed(new RuntimeException("The Server may be down."));
            }
        }
    }

    public static ResultFutureListenable wrap(ExchangeProtocol protocol, ResultFutureListener futureListener) {
        return new ResultFutureListenable(protocol, futureListener);
    }
    public static ResultFutureListenable wrap(ExchangeProtocol protocol) {
        return new ResultFutureListenable(protocol);
    }

}