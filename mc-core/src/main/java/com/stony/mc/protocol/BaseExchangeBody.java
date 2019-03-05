package com.stony.mc.protocol;

import com.stony.mc.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午4:42
 * @since 2019/1/8
 */
public abstract class BaseExchangeBody implements ExchangeBody {
    public static final String[] IMAGE_EXTENSIONS = {"ico","bmp","jpg","png","tif","gif","pcx","tga","exif","fpx","svg","psd","cdr","pcd","dxf","ufo","eps","ai","raw","WMF","webp"};
    public static final String[] VIDEO_EXTENSIONS = {"mp4","3gp","avi","mkv","wmv","mpg","vob","flv","swf","mov"};

    final ExchangeCompressEnum compress;
    byte[] value;
    byte[] lodValue;
    final String name;
    byte[] _name;
    final String key;
    byte[] _key;
    volatile boolean doCompress = false;
    BaseExchangeBody(ExchangeCompressEnum compress, byte[] value, String name, String key) {
        Objects.requireNonNull(compress, "compress must be not null");
        Objects.requireNonNull(value, "value must be not null");
        this.compress = compress;
        this.value = value;
        this.name = name;
        this.key = key;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public byte[] getNameBytes() {
        if(Utils.isEmpty(_name) && Utils.isNotEmpty(name)) {
            _name = name.getBytes(StandardCharsets.UTF_8);
        }
        return _name;
    }
    @Override
    public byte[] getValue() {
        doCompress();
        return value;
    }
    @Override
    public Object getFormatValue() {
        if(doCompress) {
            return Utils.isNotEmpty(lodValue) ? new String(lodValue, StandardCharsets.UTF_8) : null;
        }
        return Utils.isNotEmpty(value) ? new String(value, StandardCharsets.UTF_8) : null;
    }
    @Override
    public String getKey() {
        return key;
    }
    @Override
    public byte[] getKeyBytes() {
        if(Utils.isEmpty(_key) && Utils.isNotEmpty(key)) {
            _key = key.getBytes(StandardCharsets.UTF_8);
        }
        return _key;
    }
    void doCompress() {
        if (Utils.isNotEmpty(value)) {
            if(!doCompress) {
                try {
                    this.lodValue = value;
                    this.value = getCompress().compress(lodValue);
                    this.doCompress = true;
                } catch (IOException e) {
                    throw new RuntimeException(compress + "do compress data [" + Arrays.toString(lodValue) + "], error.");
                }
            }
        }
    }
    @Override
    public ExchangeCompressEnum getCompress() {
        return compress;
    }
    @Override
    public int calculateSize() {
        int size = 0;
        if (Utils.isNotEmpty(value)) {
            doCompress();
            size += (1 + 4 + value.length);
            if(Utils.isNotEmpty(name) && Utils.isNotEmpty(getNameBytes())) {
                size += (4 + _name.length);
            }
            if(Utils.isNotEmpty(key) && Utils.isNotEmpty(getKeyBytes())) {
                size += (4 + _key.length);
            }
        }
        return size;
    }
    public void setDoCompress(boolean doCompress) {
        this.doCompress = doCompress;
    }
    @Override
    public String toString() {
        return "{" +
                "value=" + getFormatValue() +
                ", name='" + getName() + '\'' +
                ", key='" + getKey() + '\'' +
                ", format='" + getFormat() +
                '}';
    }
}
