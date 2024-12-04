package org.example.cards;

import org.example.Card;

public class WeaponCard extends Card {
    private final char type;

    public WeaponCard(char type, int value) {
        super(type + String.valueOf(value), value);
        this.type = type;
    }

    public char getType() {
        return type;
    }
}
