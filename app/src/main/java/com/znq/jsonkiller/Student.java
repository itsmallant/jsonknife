package com.znq.jsonkiller;

import com.znq.nanotation.GenerateName;
import com.znq.nanotation.JSONAble;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc:
 * @author: ningqiang.zhao
 * @time: 2020-12-15 13:38
 **/
@JSONAble
public class Student {
    protected String id;
    private HashMap<String, Teacher> teachers;
    List<Book> books;
    @GenerateName("favorite_teachers")
    private Map<String, HashMap<String, Teacher>> favoriteTeachers;
    @GenerateName("favorite_books")
    Book[] favoriteBooks;


    public HashMap<String, Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(HashMap<String, Teacher> teachers) {
        this.teachers = teachers;
    }


    public Map<String, HashMap<String, Teacher>> getFavoriteTeachers() {
        return favoriteTeachers;
    }

    public void setFavoriteTeachers(Map<String, HashMap<String, Teacher>> favoriteTeachers) {
        this.favoriteTeachers = favoriteTeachers;
    }

    public Book[] getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(Book[] favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }
}
