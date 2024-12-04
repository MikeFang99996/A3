package org.example;

public abstract class Card {
    protected String name;
    protected int value;

    public Card(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName()  { return name; }
    public int    getValue() { return value; }

    @Override
    public String toString() {
        return name;
    }
}