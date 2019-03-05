package com.stony.mc.handler;

import com.stony.mc.Utils;
import com.stony.mc.protocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 上午11:43
 * @since 2019/1/4
 */
public class ExchangeCodecAdapter {

    public static final int DEFAULT_MAX_FRAME = (1024 * 1024 * 125);

    private static final Logger logger = LoggerFactory.getLogger(ExchangeCodecAdapter.class);

    private final ChannelOutboundHandler encoder;
    private final ChannelInboundHandler decoder;

    public ExchangeCodecAdapter() {
        this(DEFAULT_MAX_FRAME);
    }
    public ExchangeCodecAdapter(int maxFrameLength) {
        this.encoder = new InternalEncoder();
        this.decoder = new InternalDecoder(maxFrameLength);
    }

    //io.netty.channel.ChannelHandler
    public ChannelOutboundHandler getEncoder() {
        return encoder;
    }

    public ChannelInboundHandler getDecoder() {
        return decoder;
    }

    /**
     * <pre>
     *  {@link ChannelOutboundHandler}
     *  {@link io.netty.channel.ChannelOutboundHandlerAdapter}
     * </pre>
     */
    class InternalEncoder extends MessageToByteEncoder<ExchangeProtocol> {
        @Override
        protected void encode(ChannelHandlerContext ctx, ExchangeProtocol msg, ByteBuf out) throws Exception {
            logger.trace("encode: {}", msg);

            out.writeByte(msg.getMagic());
            out.writeLong(msg.getId());
            out.writeByte((byte) msg.getType().getCode());
            out.writeByte((byte) msg.getCompress().getCode());
            out.writeShort((short) msg.getStatus().getCode());

            int bodyLen = 0;
            ExchangeBody body = msg.getBody();

            if (Utils.isNotEmpty(body)) {
                //执行压缩
                bodyLen = body.calculateSize();
            }
            out.writeInt(bodyLen);
            //body
            if (bodyLen > 0) {
                out.writeByte((byte) body.getFormat().getCode());
                out.writeInt(body.getValue().length);
                out.writeBytes(body.getValue());
                //name
                if (Utils.isNotEmpty(body.getNameBytes())) {
                    out.writeInt(body.getNameBytes().length);
                    out.writeBytes(body.getNameBytes());
                    //key
                    if (Utils.isNotEmpty(body.getKeyBytes())) {
                        out.writeInt(body.getKeyBytes().length);
                        out.writeBytes(body.getKeyBytes());
                    }
                }
            }
        }
    }
    /**
     * <pre>
     *  {@link ChannelInboundHandler}
     *  {@link io.netty.channel.ChannelInboundHandlerAdapter}
     * </pre>
     */
    class InternalDecoder extends LengthFieldBasedFrameDecoder {

        public InternalDecoder() {
            this(DEFAULT_MAX_FRAME);
        }
        public InternalDecoder(int maxFrameLength) {
            this(maxFrameLength, 13, 4);
        }
        public InternalDecoder(
                int maxFrameLength,
                int lengthFieldOffset, int lengthFieldLength) {
            super(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
        }

        /**
         * Remember that you still need to manually
         * release any bytebufs your create by either using readBytes or readSlice,
         * as stated by the javadoc.
         * @param ctx
         * @param in
         * @return
         * @throws Exception
         */
        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            //readBytes或readSlice
            Object value = super.decode(ctx, in);
            if (value != null) {
                ByteBuf buffer = (ByteBuf) value;
                try {
                    byte magic = buffer.readByte();

                    ExchangeProtocol msg = new ExchangeProtocol(magic)
                            .id(buffer.readLong())
                            .type(buffer.readByte())
                            .compress(buffer.readByte())
                            .status(buffer.readShort());

                    int bodyLen = buffer.readInt();
                    msg.setBodyLen(bodyLen);
                    if(bodyLen > 0) {
                        ExchangeFormatEnum format = Objects.requireNonNull(ExchangeFormatEnum.byCode(buffer.readByte()));
                        int valueLen = buffer.readInt();
                        byte[] bytes = null;
                        String name = null;
                        String key = null;
                        if(valueLen > 0) {
//                        final byte[] array = Utils.getBytes(valueLen);
//                        buffer.getBytes(buffer.readerIndex(), array, 0, valueLen);
                            bytes = new byte[valueLen];
                            buffer.readBytes(bytes);
                            bodyLen -= (valueLen + 5);
                            if (!msg.getStatus().isOk()) {
                                int sc = msg.getStatus().getCode();
                                msg.status(ExchangeStatus.wrap(sc, new String(bytes, StandardCharsets.UTF_8)));
                                msg.compress(ExchangeCompressEnum.NONE);
                                logger.trace("decode: {}", msg);
                                return msg;
                            }
                            if(bodyLen > 0){
                                int nameLen = buffer.readInt();
                                if(nameLen > 0) {
                                    name = (String) buffer.readCharSequence(nameLen, StandardCharsets.UTF_8);
                                }
                                bodyLen -= (nameLen + 4);
                                if(bodyLen > 0){
                                    int keyLen = buffer.readInt();
                                    if(nameLen > 0) {
                                        key = (String) buffer.readCharSequence(keyLen, StandardCharsets.UTF_8);
                                    }
                                }
                            }
                        }
                        msg.setBody(format, bytes, name, key);
                        if (msg.getCompress() != ExchangeCompressEnum.NONE) {
                            ((BaseExchangeBody) msg.getBody()).setDoCompress(true);
                        }
                    }
                    logger.trace("decode: {}", msg);
                    return msg;
                } finally {
                    ReferenceCountUtil.release(buffer);
                }
            }
            return null;
        }
    }
}