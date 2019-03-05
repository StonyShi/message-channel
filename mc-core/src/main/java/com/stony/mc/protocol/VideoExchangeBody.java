package com.stony.mc.protocol;

import com.stony.mc.Utils;

import java.util.Objects;
import java.util.UUID;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午4:44
 * @since 2019/1/8
 */
public class VideoExchangeBody extends BaseExchangeBody {
    String extension;
    String _name;
    VideoExchangeBody(ExchangeCompressEnum compress, byte[] value, String name, String key) {
        super(compress, value, name, key);
        Objects.requireNonNull(name, "video name must be not null");
        if(Utils.isEmpty(key)) {
            if(name.contains(".")) {
                key = name.substring(name.lastIndexOf(".")+1);
            } else {
                key = name;
                name = String.format("%s.%s", UUID.randomUUID(), key);
            }
        }
        if(Utils.indexOf(VIDEO_EXTENSIONS, key) < 0) {
            throw new RuntimeException("not support video type:" + super.getName());
        }
        this._name = name;
        this.extension = key;
    }

    @Override
    public String getName() {
        return _name;
    }

    public String getExtension() {
        return extension;
    }
    @Override
    public ExchangeFormatEnum getFormat() {
        return ExchangeFormatEnum.VIDEO;
    }
    @Override
    public String toString() {
        return "{" +
                "name=" + getName() +
                ", extension='" + getExtension() +
                ", format='" + getFormat() +
                '}';
    }
}
