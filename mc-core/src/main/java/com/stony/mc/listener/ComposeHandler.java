package com.stony.mc.listener;

import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeTypeEnum;

/**
 * <p>message-channel
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午4:01
 * @since 2019/4/14
 */
public class ComposeHandler implements SubscribeListener {

    private SubscribeListener[] listeners;

    public synchronized void addListener(SubscribeListener listener) {
        if(listeners == null) {
            this.listeners = new SubscribeListener[1];
            this.listeners[0] = listener;
        } else {
            int len = listeners.length;
            SubscribeListener[] temp = new SubscribeListener[len+1];
            System.arraycopy(listeners, 0, temp, 0, len);
            temp[len] = listener;
            this.listeners = temp;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSubscribe(ExchangeProtocol value) {
        if(listeners != null) {
            for(SubscribeListener listener : listeners) {
                if(listener.support(value.getType())){
                    listener.onSubscribe(value);
                }
            }
        }
    }

    @Override
    public boolean support(ExchangeTypeEnum typeEnum) {
        if(listeners != null) {
            for(SubscribeListener listener : listeners) {
                if(listener.support(typeEnum)){
                    return true;
                }
            }
        }
        return false;
    }
}
