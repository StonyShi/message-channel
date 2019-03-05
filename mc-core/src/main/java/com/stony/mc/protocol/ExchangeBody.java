package com.stony.mc.protocol;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午4:42
 * @since 2019/1/8
 */
public interface ExchangeBody {
    ExchangeFormatEnum getFormat();
    byte[] getValue();
    <T> T getFormatValue();
    String getName();
    byte[] getNameBytes();
    String getKey();
    byte[] getKeyBytes();
    int calculateSize();
    ExchangeCompressEnum getCompress();
}