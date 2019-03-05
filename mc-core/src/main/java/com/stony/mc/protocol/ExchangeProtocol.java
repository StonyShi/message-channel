package com.stony.mc.protocol;

import com.stony.mc.manager.SubscribeInfo;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午12:08
 * @since 2019/1/3
 */
public class ExchangeProtocol implements Serializable {
    private static final long serialVersionUID = -600200650940898647L;
    final byte magic = 0x71;       //1
    protected long id;                       //8
    protected ExchangeTypeEnum type;         //1
    protected ExchangeCompressEnum compress; //1
    protected ExchangeStatus status;         //2
    protected int bodyLen;                   //4

    protected ExchangeBody body;

    public static ExchangeProtocol subscribe(long id, String group, String topic, String key) {
        Objects.requireNonNull(id, "id must be not null");
        Objects.requireNonNull(topic, "topic must be not null");
        ExchangeProtocol value = new ExchangeProtocol()
                .id(id)
                .type(ExchangeTypeEnum.SUBSCRIBE)
                .compress(ExchangeCompressEnum.NONE)
                .status(ExchangeStatus.OK);
        value.setBody(ExchangeFormatEnum.JSON, new SubscribeInfo(group, topic, key));
        return value;
    }

    public static ExchangeProtocol subscribe(long id, String topic, String key) {
        return subscribe(id, null, topic, key);
    }

    public static ExchangeProtocol unsubscribe(long id, String group, String topic, String key) {
        Objects.requireNonNull(id, "id must be not null");
        Objects.requireNonNull(topic, "topic must be not null");
        ExchangeProtocol value = new ExchangeProtocol()
                .id(id)
                .type(ExchangeTypeEnum.UNSUBSCRIBE)
                .compress(ExchangeCompressEnum.NONE)
                .status(ExchangeStatus.OK);
        value.setBody(ExchangeFormatEnum.JSON, new SubscribeInfo(group, topic, key));
        return value;
    }

    public static ExchangeProtocol unsubscribe(long id, String topic, String key) {
        return unsubscribe(id, null, topic, key);
    }

    public static ExchangeProtocol ping(long id) {
        Objects.requireNonNull(id, "id must be not null");
        return new ExchangeProtocol()
                .id(id)
                .type(ExchangeTypeEnum.PING)
                .compress(ExchangeCompressEnum.NONE)
                .status(ExchangeStatus.OK);
    }
    public static ExchangeProtocol ack(long id) {
        Objects.requireNonNull(id, "id must be not null");
        return new ExchangeProtocol()
                .id(id)
                .type(ExchangeTypeEnum.ACK)
                .compress(ExchangeCompressEnum.NONE)
                .status(ExchangeStatus.OK);
    }
    public static ExchangeProtocol create(long id) {
        Objects.requireNonNull(id, "id must be not null");
        return new ExchangeProtocol()
                .id(id)
                .type(ExchangeTypeEnum.MESSAGE)
                .compress(ExchangeCompressEnum.NONE)
                .status(ExchangeStatus.OK);
    }
    public static ExchangeProtocol allocation(long id, boolean all) {
        Objects.requireNonNull(id, "id must be not null");
        if(all) {
            return ExchangeProtocol
                    .create(id)
                    .type(ExchangeTypeEnum.ALLOCATION)
                    .text("true", null, null);
        }
        return ExchangeProtocol
                .create(id)
                .type(ExchangeTypeEnum.ALLOCATION);
    }
    private ExchangeProtocol() {}
    public ExchangeProtocol(byte _magic) {
        if(_magic != this.magic) {
            throw new RuntimeException("Protocol not parse.");
        }
    }
    public ExchangeProtocol(byte _magic, long id, ExchangeTypeEnum type, ExchangeCompressEnum compress, ExchangeStatus status, int bodyLen) {
        if(_magic != this.magic) {
            throw new RuntimeException("Protocol not parse.");
        }
        Objects.requireNonNull(id, "id must be not null");
        this.id = id;
        this.type = type;
        this.compress = compress;
        this.status = status;
        this.bodyLen = bodyLen;
    }

    public ExchangeProtocol id(long id) {
        this.id = id;
        return this;
    }

    public ExchangeProtocol type(ExchangeTypeEnum type) {
        this.type = Objects.requireNonNull(type, "type must be not null");
        return this;
    }
    public ExchangeProtocol type(byte code) {
        return this.type(ExchangeTypeEnum.byCode(code));
    }

    public ExchangeProtocol compress(ExchangeCompressEnum compress) {
        this.compress = Objects.requireNonNull(compress, "compress must be not null");
        return this;
    }
    public ExchangeProtocol compress(byte code) {
        return this.compress(ExchangeCompressEnum.byCode(code));
    }

    public ExchangeProtocol status(ExchangeStatus status) {
        this.status = Objects.requireNonNull(status, "status must be not null");
        if(!status.isOk()) {
            textBody(status.getReasonPhrase(), null, null);
        }
        return this;
    }
    public ExchangeProtocol status(short code) {
        return this.status(ExchangeStatus.valueOf(code));
    }
    public ExchangeProtocol textBody(String value, String name, String key) {
        this.body = new TextExchangeBody(getCompress(), value, name, key);
        return this;
    }
    public ExchangeProtocol jsonBody(String value, String name, String key) {
        this.body = new JsonExchangeBody(getCompress(), value, name, key);
        return this;
    }
    public byte getMagic() {
        return magic;
    }

    public long getId() {
        return id;
    }

    public ExchangeTypeEnum getType() {
        return type;
    }

    public ExchangeStatus getStatus() {
        return status;
    }

    public int getBodyLen() {
        return bodyLen;
    }

    public ExchangeCompressEnum getCompress() {
        return compress;
    }

    public void setBodyLen(int bodyLen) {
        this.bodyLen = bodyLen;
    }

    public void setBody(ExchangeBody body) {
        this.body = body;
    }

    public ExchangeBody getBody() {
        return body;
    }
    public ExchangeProtocol json(byte[] value, String name, String key) {
        setBody(ExchangeFormatEnum.JSON, value, name, key);
        return this;
    }
    public ExchangeProtocol json(String value, String name, String key) {
        setBody(ExchangeFormatEnum.JSON, value.getBytes(StandardCharsets.UTF_8), name, key);
        return this;
    }
    public ExchangeProtocol text(String value, String name, String key) {
        setBody(ExchangeFormatEnum.TEXT, value.getBytes(StandardCharsets.UTF_8), name, key);
        return this;
    }
    public <V> void setBody(ExchangeFormatEnum format, V v) {
        setBody(format, format.serialize(v), null, null);
    }
    public void setBody(ExchangeFormatEnum format, String value, String name, String key) {
        setBody(format, value.getBytes(StandardCharsets.UTF_8), name, key);
    }
    public void setBody(ExchangeFormatEnum format, byte[] value, String name, String key) {
        switch (format) {
            case TEXT:
                this.body = new TextExchangeBody(getCompress(), value, name, key);
                break;
            case JSON:
                this.body = new JsonExchangeBody(getCompress(), value, name, key);
                break;
            default:
                this.body = new EmptyExchangeBody();
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeProtocol that = (ExchangeProtocol) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ExchangeProtocol{" +
                "magic=" + magic +
                ", id=" + id +
                ", type=" + type +
                ", compress=" + compress +
                ", status=" + status +
                ", bodyLen=" + bodyLen +
                ", body=" + body +
                '}';
    }

}