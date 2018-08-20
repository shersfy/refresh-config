package org.onosproject.config.refresh.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定属性值，支持表达式：如"#{systemProperties.myProp}"
 * 2018年8月17日
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    
    /**
     * 指定属性值，支持表达式：如"${filename.key}", filename配置文件名
     * @return
     */
    String value();

}
