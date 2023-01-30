package ru.job4j.di;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class StartUI {
    @Autowired
    private Store store;
    @Autowired
    private ConsoleInput input;

    public void setStore(Store store) {
        this.store = store;
    }

    public void setInput(ConsoleInput input) {
        this.input = input;
    }

    public void add(String value) {
        store.add(value);
    }

    public String askStr(String question) {
        return input.askStr(question);
    }

    public void print() {
        for (String value : store.getAll()) {
            System.out.println(value);
        }
    }
}
