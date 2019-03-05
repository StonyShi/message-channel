package com.stony.mc.listener;

import com.stony.mc.protocol.ExchangeProtocol;

/**
 * <p>mc-core
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午2:22
 * @since 2019/1/10
 */
public interface ResultFutureListener<T extends ExchangeProtocol> {
    void onSuccess(T t);
    void onFailed(Throwable e);
}
