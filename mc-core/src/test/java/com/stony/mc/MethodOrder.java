package com.stony.mc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午2:38
 * @since 2019/1/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MethodOrder {

    int value() default 0;
}
