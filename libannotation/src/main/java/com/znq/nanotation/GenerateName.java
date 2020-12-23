package com.znq.nanotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @desc:
 * @author: ningqiang.zhao
 * @time: 2020-12-14 18:07
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface GenerateName {
    String value();
}
