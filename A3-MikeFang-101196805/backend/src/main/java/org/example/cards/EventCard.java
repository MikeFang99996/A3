package org.example.cards;

import org.example.Card;

public class EventCard extends Card {
    private final EventType eventType;

    public EventCard(EventType eventType) {
        super(eventType.name(), 0);
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }
}