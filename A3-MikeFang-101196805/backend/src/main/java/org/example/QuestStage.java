package org.example;

import org.example.cards.FoeCard;
import org.example.cards.WeaponCard;
import java.util.ArrayList;
import java.util.List;

public class QuestStage {
    private List<Card> cards;

    public QuestStage() {
        this.cards = new ArrayList<>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public int getValue() {
        int sum = 0;
        for (Card card : cards) {
            sum += card.getValue();
        }
        return sum;
    }

    public boolean canAddCard(Card card) {
        if (card instanceof FoeCard) {
            return cards.stream().noneMatch(c -> c instanceof FoeCard);
        } else if (card instanceof WeaponCard) {
            return cards.stream().noneMatch(c -> c.getName().equals(card.getName()));
        }
        return false;
    }
}
