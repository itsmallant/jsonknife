package com.znq.jsonkiller;

import com.znq.annotation.GenerateName;
import com.znq.annotation.JSONAble;

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

    int id;

    public Teacher() {
    }

    public Teacher(String name, String clazz) {
        this.name = name;
        this.clazz = clazz;
    }
}
