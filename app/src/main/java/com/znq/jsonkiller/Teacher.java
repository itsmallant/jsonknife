package com.znq.jsonkiller;

import com.znq.nanotation.GenerateName;
import com.znq.nanotation.JSONAble;

/**
 * @desc:
 * @author: ningqiang.zhao
 * @time: 2020-12-15 14:49
 **/
@JSONAble
public class Teacher {
    String name;
    @GenerateName("class")
    String clazz;

    public Teacher() {
    }

    public Teacher(String name, String clazz) {
        this.name = name;
        this.clazz = clazz;
    }
}
