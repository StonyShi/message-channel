package com.stony.mc.protocol;

import com.stony.mc.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午1:43
 * @since 2019/1/7
 */
public enum ExchangeCompressEnum {
    NONE(0) {
        @Override
        byte[] compress(byte[] data) throws IOException {
            return data;
        }

        @Override
        byte[] decompress(byte[] data) throws IOException {
            return data;
        }
    }, GZIP(1) {
        @Override
        byte[] compress(byte[] data) throws IOException {
            return Utils.gzip(data);
        }

        @Override
        byte[] decompress(byte[] data) throws IOException {
            return Utils.ungzip(data);
        }
    }, LZ4(2) {
        @Override
        byte[] compress(byte[] data) throws IOException {
            return Utils.lz4(data);
        }

        @Override
        byte[] decompress(byte[] data) throws IOException {
            return Utils.unlz4(data);
        }
    }, SNAPPY(3) {
        @Override
        byte[] compress(byte[] data) throws IOException {
            return Utils.snappy(data);
        }

        @Override
        byte[] decompress(byte[] data) throws IOException {
            return Utils.unsnappy(data);
        }
    };
    int code;

    ExchangeCompressEnum(int code) {
        this.code = code;
    }

    static Map<Integer, ExchangeCompressEnum> INDEX;

    static {
        INDEX = new HashMap<>(8);
        for (ExchangeCompressEnum v : ExchangeCompressEnum.values()) {
            INDEX.put(v.code, v);
        }
    }
    public static ExchangeCompressEnum byCode(int code) {
        return INDEX.get(code);
    }

    public int getCode() {
        return code;
    }

    abstract byte[] compress(byte[] data) throws IOException;

    abstract byte[] decompress(byte[] data) throws IOException;
}
