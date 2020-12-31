# JsonKiller
使用APT，将注解的java对象生成JSONObject。

## gradle配置：
```
repositories {
			...
			maven { url 'https://jitpack.io' }
		}
        
dependencies {
	implementation 'com.github.itsmallant.jsonknife:jsonknife-annotation:1.0.0'
    implementation 'com.github.itsmallant.jsonknife:jsonknife-runtime:1.0.0'
    kapt 'com.github.itsmallant.jsonknife:jsonknife-compiler:1.0.0'
	}        
        
```

## 使用说明：

1. 在需要转化为JSONObject的Java对象想打上注解`@JSONAble`:
```
@JSONAble
public class Student {
    protected String id;
    ....
}

```

2. 使用`JsonKnife.convert`方法将Java对象转化为JSONObject对象：
```
Student student = new Student();
//...设置student相关属性
JSONObject convert = JsonKnife.convert(student);

```

## 混淆配置：

```

 -keep public class **.JSONFactory{
    *;
 }
-keepnames @com.znq.nanotation.JSONAble class * {}


```


## 注解说明：

`JSONAble`：标注在需要生成JSONObject对象的Java Bean上；

`Exclude`：标注在需要忽略的Java对象的属性上，被标注后的属性不会被转化到JSONObject中；

`GenerateName`：如果某个Java属性在转化到JSONObject中的Key需要制定名称，将该注解标注在对应的属性上。如果没有标注，则使用属性的名称。

示例：

简单的Java对象

```
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
```

生成的JSON对象：

```
 {"name":"张老师","class":"数学"}
```

目前支持List、Map中的对象嵌套（Map只支持Value的嵌套）。含有嵌套的Java对象示例：
```
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
```

生成的Json对象（为方便观看，对Json进行了格式化）：

```
{
    "id":"007",
    "teachers":{
        "Nature":{
            "name":"王五",
            "class":"自然老师"
        },
        "English":{
            "name":"李四",
            "class":"英语"
        },
        "生物":{
            "name":"赵六",
            "class":"生物老师"
        },
        "Chinese":{
            "name":"张三",
            "class":"语文"
        }
    },
    "books":[
        {
            "book_name":"自然哲学的数学原理",
            "book_authors":"[牛顿]"
        },
        {
            "book_name":"明朝那些事",
            "book_authors":"[当年明月]"
        },
        {
            "book_name":"乱世枭雄",
            "book_authors":"[单田芳]"
        }
    ],
    "favorite_teachers":{
        "课内":{
            "English":{
                "name":"李四",
                "class":"英语"
            },
            "Chinese":{
                "name":"张三",
                "class":"语文"
            }
        },
        "课外":{

        }
    },
    "favorite_books":[
        {
            "book_name":"仙逆",
            "book_authors":"[六耳]"
        },
        {
            "book_name":"凡人修仙传",
            "book_authors":"[忘语]"
        }
    ]
}

```

