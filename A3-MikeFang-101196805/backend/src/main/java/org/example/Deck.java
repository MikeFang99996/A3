package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;
    private List<Card> discardPile;

    public Deck() {
        cards = new ArrayList<>();
        discardPile = new ArrayList<>();
    }

    public List<Card> getCards()    {return cards; }
    public List<Card> getDiscards() {return discardPile; }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void addDiscard(Card card) {
        discardPile.add(card);
    }

    public void addDiscards(List<Card> cards) {
        discardPile.addAll(cards);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    private void reshuffleDiscardPile() {
        cards.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public Card drawCard() {
        if (isEmpty()) {
            reshuffleDiscardPile();
        }
        return isEmpty() ? null : cards.remove(cards.size() - 1);
    }

    public void addToLast(Card card) {
        cards.add(card);
    }
}
