package com.znq.jsonkiller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.znq.runtime.JsonKnife;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void book(View view) {
        Book book = new Book("语文", Arrays.asList("鲁迅", "朱自清"));
        JSONObject convert = JsonKnife.convert(book);
        Log.d(TAG, "book = \r\n" + convert);
    }

    public void teacher(View view) {
        Teacher teacher = new Teacher();
        teacher.name = "张老师";
        teacher.clazz = "数学";
        JSONObject convert = JsonKnife.convert(teacher);
        Log.d(TAG, "teacher = \r\n" + convert);
    }

    public void student(View view) {
        Student student = new Student();
        student.id = "007";

        HashMap<String, Teacher> teachers = new HashMap<>();
        teachers.put("Chinese", new Teacher("张三", "语文"));
        teachers.put("English", new Teacher("李四", "英语"));

        student.setTeachers(teachers);

        List authors = new ArrayList();
        authors.add("牛顿");
        List<Book> books = Arrays.asList(new Book("自然哲学的数学原理", authors),
                new Book("明朝那些事", Arrays.asList("当年明月")),
                new Book("乱世枭雄", Arrays.asList("单田芳"))
        );

        student.books = books;


        Map<String, HashMap<String, Teacher>> favoriteTeachers = new HashMap<>();
        HashMap<String, Teacher> teachers1 = new HashMap<>();
        teachers.put("Nature", new Teacher("王五", "自然老师"));
        teachers.put("生物", new Teacher("赵六", "生物老师"));

        HashMap<String, Teacher> teachers2 = new HashMap<>();
        teachers2.put("Chinese", new Teacher("张三", "语文"));
        teachers2.put("English", new Teacher("李四", "英语"));

        favoriteTeachers.put("课外", teachers1);
        favoriteTeachers.put("课内", teachers2);

        student.setFavoriteTeachers(favoriteTeachers);

        student.favoriteBooks = new Book[]{
                new Book("仙逆", Arrays.asList("六耳")),
                new Book("凡人修仙传", Arrays.asList("忘语"))};

        JSONObject convert = JsonKnife.convert(student);
        Log.d(TAG, "student = \r\n" + convert);
    }
}
