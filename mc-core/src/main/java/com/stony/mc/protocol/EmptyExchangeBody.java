package com.stony.mc.protocol;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午4:42
 * @since 2019/1/8
 */
public class EmptyExchangeBody extends BaseExchangeBody {
    EmptyExchangeBody() {
        super(null, null, null, null);
    }
    @Override
    public ExchangeFormatEnum getFormat() {
        return ExchangeFormatEnum.NONE;
    }
    @Override
    public byte[] getValue() {
        return null;
    }
    @Override
    public String getName() {
        return null;
    }
    @Override
    public byte[] getNameBytes() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }
    @Override
    public byte[] getKeyBytes() {
        return null;
    }
    @Override
    public int calculateSize() {
        return 0;
    }
    @Override
    public String toString() {
        return "{format:NONE}";
    }
}
