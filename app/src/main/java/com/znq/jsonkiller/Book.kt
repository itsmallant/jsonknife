package com.znq.jsonkiller

import com.znq.nanotation.GenerateName
import com.znq.nanotation.JSONAble

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
    companion object {
        private const val TAG = "Book"
    }
}