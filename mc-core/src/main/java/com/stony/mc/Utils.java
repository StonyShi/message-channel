package com.stony.mc;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.PlatformDependent;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午4:16
 * @since 2019/1/3
 */
public abstract class Utils {

    public static boolean isEmpty(Object v) {
        return v == null;
    }
    public static boolean isNotEmpty(Object v) {
        return !isEmpty(v);
    }

    public static boolean isNotEmpty(byte[] v) {
        return !isEmpty(v);
    }
    public static boolean isNotEmpty(String v) {
        return !isEmpty(v);
    }

    public static boolean isEmpty(byte[] v) {
        return v == null || v.length == 0;
    }
    public static boolean isEmpty(String v) {
        return v == null || v.isEmpty();
    }

    public static int calculateSize(byte[] value) {
        if(Utils.isNotEmpty(value)) {
            return value.length;
        }
        return 0;
    }
    public static byte[] getBytes(int len) {
        final byte[] array;
        if (len <= 1024) {
            array = BYTE_ARRAYS.get();
        } else {
            array = PlatformDependent.allocateUninitializedArray(len);
        }
        return array;
    }

    static final FastThreadLocal<byte[]> BYTE_ARRAYS = new FastThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() throws Exception {
            return PlatformDependent.allocateUninitializedArray(1024);
        }
    };




    public static byte[] ungzip2(byte[] data) {
        Objects.requireNonNull(data);
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                byte[] buf = new byte[1024];
                int num = -1;
                while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                    baos.write(buf, 0, num);
                }
                b = baos.toByteArray();
                baos.flush();
            } finally {
                baos.close();
                gzip.close();
                bis.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }
    public static byte[] gzip2(byte[] data) {
        Objects.requireNonNull(data);
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            try {
                gzip.write(data);
                gzip.finish();
            }finally {
                gzip.close();
            }
            try {
                b = bos.toByteArray();
            }finally {
                bos.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }
    public static byte[] gzip(byte[] data) throws IOException {
        Objects.requireNonNull(data);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            OutputStream outputStream = null;
            try {
                outputStream = new GzipCompressorOutputStream(out);
                outputStream.write(data);
                outputStream.flush();
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            return out.toByteArray();
        }
    }

    public static byte[] ungzip(byte[] data) throws IOException {
        Objects.requireNonNull(data);
        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
             InputStream inputStream = new GzipCompressorInputStream(in, true)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    public static byte[] lz4(byte[] data) throws IOException {
        Objects.requireNonNull(data);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            OutputStream outputStream = null;
            try {
                outputStream = new FramedLZ4CompressorOutputStream(out);
                outputStream.write(data);
                outputStream.flush();
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            return out.toByteArray();
        }
    }
    public static byte[] unlz4(byte[] data) throws IOException {
        Objects.requireNonNull(data);
        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
             InputStream inputStream = new FramedLZ4CompressorInputStream(in, true)) {
            return IOUtils.toByteArray(inputStream);
        }
    }
    public static byte[] snappy(byte[] data) throws IOException {
        Objects.requireNonNull(data);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            OutputStream outputStream = null;
            try {
                outputStream = new FramedSnappyCompressorOutputStream(out);
                outputStream.write(data);
                outputStream.flush();
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            return out.toByteArray();
        }
    }
    public static byte[] unsnappy(byte[] data) throws IOException {
        Objects.requireNonNull(data);
        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
             InputStream inputStream = new FramedSnappyCompressorInputStream(in)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    public static int indexOf(String[] array, String o) {
        Objects.requireNonNull(array);
        int size = array.length;
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (array[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(array[i]))
                    return i;
        }
        return -1;
    }



    public static String toLine(String str) {
        char[] array = str.toCharArray();
        char[] arr2 = new char[array.length*2];
        int index = 0;
        for (int i = 0; i < array.length; i++) {
            char c = array[i];
            if ('A' <= c && c <= 'Z') {
                array[i] = '_';
                if(index > 0) {
                    arr2[index++] = '_';
                }
                arr2[index++] = c;
            } else {
                arr2[index++] = c;
            }
        }
        return String.valueOf(Arrays.copyOf(arr2, index));
    }
    public static String toCamel(String str) {
        char[] array = str.toLowerCase().toCharArray();
        char[] arr2 = new char[array.length];
        int index = 1;
        arr2[0] = array[0];
        boolean next = false;
        for (int i = 1; i < array.length; i++) {
            char c = array[i];
            if(c == '_') {
                next = true;
            } else {
                if(next){
                    arr2[index++] = (char)(c-32);
                }else {
                    arr2[index++] = c;
                }
                next = false;

            }
        }
        if (arr2[0] >= 'a' && arr2[0] <= 'z') {
            arr2[0] -= 32;
        }
        return String.valueOf(Arrays.copyOf(arr2, index));
    }
    public static String toCamel2(String v) {
        if(v.contains("_")) {
            char[] chars = v.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if(c == '_') {
                    chars[i+1] -= 32;
                }

            }
            return new String(chars).replace("_", "");
        }
        return v;
    }
}
