package com.znq.jsonkiller

import com.znq.annotation.GenerateName
import com.znq.annotation.JSONAble

/**
 *@desc:
 *@author: ningqiang.zhao
 *@time: 2020-12-15 15:00
 **/
@JSONAble
class Book(@GenerateName("book_name")
           val name: String,
           @GenerateName("book_authors")
           val authors: List<String>
) {
    var id:Int = 0
    var is_Ok = false
    companion object {
        private const val TAG = "Book"
    }
}