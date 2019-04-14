package com.stony.mc;

import com.stony.mc.listener.BalanceListener;
import com.stony.mc.listener.BusinessHandler;
import com.stony.mc.listener.ComposeHandler;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeProtocolContext;
import com.stony.mc.protocol.ExchangeResponseConsumer;
import com.stony.mc.protocol.ExchangeTypeEnum;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ComposeHandlerTest {
    public static void main(String[] args) {
        ComposeHandler handler = new ComposeHandler();
        handler.addListener(new BalanceListener() {
            @Override
            public void onBalance(Integer value) {
                System.out.println("------------ balance ----------------------" + value);
            }
        });
        handler.addListener(new BusinessHandler() {
            @Override
            public ExchangeProtocol handle(ExchangeProtocolContext request) {
                System.out.println("------------ business ---------------------" + request);
                return ExchangeProtocol.ack(234324234);
            }
            @Override
            public boolean support(ExchangeTypeEnum typeEnum) {
                return ExchangeTypeEnum.CHAT == typeEnum;
            }
        });
        System.out.println(handler.support(ExchangeTypeEnum.BALANCE));
        System.out.println(handler.support(ExchangeTypeEnum.CHAT));

        handler.onSubscribe(ExchangeProtocol.create(1212312).type(ExchangeTypeEnum.BALANCE).text("11",null, null));

        handler.onSubscribe(new ExchangeResponseConsumer(
                ExchangeProtocol.create(1212312)
                        .type(ExchangeTypeEnum.CHAT)
                        .text("11", "11", "11"), new ChannelHandlerContextMock()) {
            @Override
            public void accept(ChannelHandlerContext curCtx, ExchangeProtocol vv) {
                System.out.println("------- accept : " + vv);
            }
        });
    }
}
