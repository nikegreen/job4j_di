# Проект job4j_di
## DI - dependency injection  "внедрение зависимостей"

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
У меня загружались классы: Store.class, ConsoleInput.class, StartUI.class.
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
