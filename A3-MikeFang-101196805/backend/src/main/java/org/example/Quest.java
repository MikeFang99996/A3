package org.example;

import org.example.cards.QuestCard;
import java.util.ArrayList;
import java.util.List;

public class Quest {
    private QuestCard questCard;
    private Player sponsor;
    private List<QuestStage> stages;

    public Quest(QuestCard questCard, Player sponsor) {
        this.questCard = questCard;
        this.sponsor = sponsor;
        this.stages = new ArrayList<>();
        for (int i = 0; i < questCard.getStages(); i++) {
            stages.add(new QuestStage());
        }
    }

    public Player           getSponsor()    { return sponsor; }
    public QuestCard        getQuest()      { return questCard; }
    public List<QuestStage> getStages()     { return stages; }
    public QuestStage       getStage(int i) { return stages.get(i); }
}
