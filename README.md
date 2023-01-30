# Проект job4j_di
## DI - dependency injection  "внедрение зависимостей" и Spring

### Идея DI.

1. Есть хранилище объектов. В нем мы будем регистрировать классы, объекты которых хотим иметь в проекте.

В нашем примере мы зарегистрируем там Tracker, StartUI, ConsoleInput.

2. Хранилище в рамках DI называется Context, то есть объекты относящиеся к предметной области нашего проекта.

3. После регистрации классов Context начинает инициализацию объектов. Он строит дерево зависимостей. Сначала он создает объекты без зависимостей.

А потом создаем объекты с зависимостями.

4. После инициализации программист может получить нужный объект из Context.


Для реализации DI используется два подхода: мета программирование, рефлексия.

В этом проекте используется рефлексия. Рефлексия позволяет узнать какие элементы имеет класс в процессе выполнения программы.

### Class Context
``` java
public <T> T get(Class<T> inst)<br>
```
Предназначен для создания объектов класса. Классы должны быть зарегистрированы функцией:<br>
``` java
public void reg(Class cl)<br>
```
Внутри функции получаем список конструкторов<br>
Constructor[] constructors = cl.getDeclaredConstructors();
Проверяем количество конструкторов. Должен быть один коструктор.<br>
``` java
if (constructors.length > 1) {
    throw new IllegalStateException(
        "Class has multiple constructors : " + cl.getCanonicalName());
}
```
Получаем список аргументов в конструкторе.<br>
Для каждого аргумента по типу ищем зарегистрированный тип.
``` java
for (Class arg : con.getParameterTypes()) {
    if (!els.containsKey(arg.getCanonicalName())) {
        throw new IllegalStateException(
        "Object doesn't found in context : " + arg.getCanonicalName());
    }
    args.add(els.get(arg.getCanonicalName()));
}
```
Получим список аргументов для конструктора.
Добавляем тип в множество.
``` java
try {
    els.put(cl.getCanonicalName(), con.newInstance(args.toArray()));
} catch (Exception e) {
    throw new IllegalStateException(
        "Coun't create an instance of : " + cl.getCanonicalName(), e);
}
``` 
Пример использования в файле 'Main.java'. Тоже самое делает spring класс AnnotationConfigApplicationContext();.
``` java
package ru.job4j.di;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringDI {
public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(Store.class);
    context.register(ConsoleInput.class);
    context.register(StartUI.class);
    context.refresh();
    StartUI ui = context.getBean(StartUI.class);
    ui.add("Petr Arsentev");
    ui.add("Ivan ivanov");
    ui.print();
    }
}
```
Прописывать вручную классы для регистрации неудобно, т.к. можно забыть зарегистрировать класс.
Можно автоматически сканировать проект с помощью функции context.scan("ru.job4j.di");.
Функция имеет аргумент - путь к папке, которую надо просканировать. Классы надо пометить @Component.
@Component нужно объявить:
``` java
import org.springframework.stereotype.Component;
```
У меня загружались классы: Store, ConsoleInput, StartUI.
Пример как нужно добавить пометить класс Store.class (файл Store.java)
``` java
package ru.job4j.di;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Store {
    private final List<String> data = new ArrayList<String>();

    public void add(String value) {
        data.add(value);
    }

    public List<String> getAll() {
        return data;
    }
}
```

Вместо строк
``` java
context.register(Store.class);
context.register(ConsoleInput.class);
context.register(StartUI.class);
```
поставим строку
``` java
context.scan("ru.job4j.di");
```


Spring по умолчанию загружает бины в режиме синглтон, то есть один объект на виртуальную машину.
В bean можно прописать режим создания объекта с помощью '@Scope()'.
Для подключения аннотации @Scope() нужно импортировать:
``` java
import org.springframework.context.annotation.Scope;
```
Spring поддерживаем 6 режимов.
1. singleton - объект создает один раз на всю виртуальную машину.

2. prototype - каждый раз создается новый объект.

3. session - объект существует, пока существует сессия пользователя.

4. request - объект существует, пока существует запрос.

5. application - объект существует, пока существует ServletContext.

6. websocket - объект существует, пока есть активная сессия для web-сокета.

Например, если прописать @Scope("prototype") у классов Store, ConsoleInput, StartUI, то
слудущий код (файл SpringDI.java):<br>
``` java
package ru.job4j.di;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringDI {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("ru.job4j.di");
        context.refresh();
        StartUI ui = context.getBean(StartUI.class);
        ui.add("Petr Arsentev");
        ui.add("Ivan ivanov");
        ui.print();
        System.out.println("---- ui2 ----");
        StartUI ui2 = context.getBean(StartUI.class);
        ui2.add("Ivan ivanov");
        ui2.print();
    }
}
```
Программа выведет:
``` text
"C:\Program Files\Java\jdk-19\bin\java.exe" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2022.2.4\lib\idea_rt.jar=5066:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2022.2.4\bin" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath C:\projects\job4j_di\target\classes;C:\Users\nikez\.m2\repository\org\springframework\spring-core\5.3.3\spring-core-5.3.3.jar;C:\Users\nikez\.m2\repository\org\springframework\spring-jcl\5.3.3\spring-jcl-5.3.3.jar;C:\Users\nikez\.m2\repository\org\springframework\spring-beans\5.3.3\spring-beans-5.3.3.jar;C:\Users\nikez\.m2\repository\org\springframework\spring-context\5.3.3\spring-context-5.3.3.jar;C:\Users\nikez\.m2\repository\org\springframework\spring-aop\5.3.3\spring-aop-5.3.3.jar;C:\Users\nikez\.m2\repository\org\springframework\spring-expression\5.3.3\spring-expression-5.3.3.jar ru.job4j.di.SpringDI
Petr Arsentev
Ivan ivanov
---- ui2 ----
Ivan ivanov

Process finished with exit code 0
```
На консоли для ui2 не будет выведен текст 'Petr Arsentev', потому что объект Store будет вызван всегда новый и мыдобавили только 'Ivan ivanov'.
