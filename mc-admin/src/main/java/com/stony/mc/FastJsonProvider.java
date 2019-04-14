package com.stony.mc;


import com.alibaba.fastjson.JSON;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes({"application/json", "application/json;charset=UTF-8", "text/json"})
@Produces({"application/json", "application/json;charset=UTF-8", "text/json"})
public class FastJsonProvider extends AbstractMessageReaderWriterProvider<Object> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (!isJsonType(mediaType)) {
            return false;
        }
        return true;
    }

    @Override
    public Object readFrom(Class<Object> aClass, Type genericType, Annotation[] annotations,
                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                           InputStream inputStream) throws IOException, WebApplicationException {
        return JSON.parseObject(inputStream, genericType);
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (!isJsonType(mediaType)) {
            return false;
        }
        return true;
    }

    @Override
    public void writeTo(Object value, Class<?> aClass, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream outputStream) throws IOException, WebApplicationException {

        JSON.writeJSONString(outputStream, value);
    }



    protected boolean isJsonType(MediaType mediaType) {
        if (mediaType != null) {
            // Ok: there are also "xxx+json" subtypes, which count as well
            String subtype = mediaType.getSubtype();
            return "json".equalsIgnoreCase(subtype) || subtype.endsWith("+json");
        }
        /* Not sure if this can happen; but it seems reasonable
         * that we can at least produce json without media type?
         */
        return true;
    }

}
