package org.example.cards;

import org.example.Card;

public class QuestCard extends Card {
    private final int stages;

    public QuestCard(int stages) {
        super("Q" + stages, stages);
        this.stages = stages;
    }

    public int getStages() { return stages; }

    @Override
    public int getValue() { return getStages(); }
}