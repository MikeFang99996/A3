package org.example;

import org.example.cards.*;
import java.util.*;

public class Player {
    private final String name;
    private List<Card> hand;
    private int shields;
    private Queue<String> playerResponses;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.shields = 0;
        this.playerResponses = new LinkedList<>();
    }

    public String     getName()    { return name; }
    public List<Card> getHand()    { return hand; }
    public int        getShields() { return shields; }
    public int        getIndex()   { return Integer.parseInt(name.substring(1)); }

    public void setHand(List<Card> hand) { this.hand = hand; }

    public void setResponses(String... responses) {
        playerResponses.addAll(Arrays.asList(responses));  // Add responses to this player's queue
    }

    public String getInput() {
        if (!playerResponses.isEmpty()) {
            System.out.println(playerResponses.peek());
            return playerResponses.poll();
        }
        return new Scanner(System.in).nextLine();
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void addCards(List<Card> cards) {
        hand.addAll(cards);
    }

    public void addShields(int amount) {
        shields += amount;
    }

    public void removeShields(int amount) {
        shields = Math.max(0, shields - amount);
    }

    public void discardCards(List<Card> cards) {
        hand.removeAll(cards);
        System.out.println("\nAll cards used for attacking are discarded");
    }

    public void sortHand() {
        hand.sort(new Comparator<Card>() {
            @Override
            public int compare(Card card1, Card card2) {
                if (card1 instanceof FoeCard && card2 instanceof WeaponCard) {
                    return -1;
                } else if (card1 instanceof WeaponCard && card2 instanceof FoeCard) {
                    return 1;
                } else if (card1 instanceof FoeCard && card2 instanceof FoeCard) {
                    return Integer.compare(card1.getValue(), card2.getValue());
                } else if (card1 instanceof WeaponCard weapon1 && card2 instanceof WeaponCard weapon2) {
                    if (weapon1.getType() == 'S' && weapon2.getType() == 'H') {
                        return -1;
                    } else if (weapon1.getType() == 'H' && weapon2.getType() == 'S') {
                        return 1;
                    } else {
                        return Integer.compare(weapon1.getValue(), weapon2.getValue());
                    }
                }
                return 0;
            }
        });
    }

    public List<Card> trimHand() {
        sortHand();
        List<Card> discardedCards = new ArrayList<>();

        while (hand.size() > 12) {
            System.out.println(name + "'s hand: " + hand);
            System.out.println("You must discard " + (hand.size() - 12) + " card(s). Enter the positions of the cards to discard, separated by spaces:");

            String input = getInput();
            String[] positions = input.split(" ");

            List<Integer> validPositions = new ArrayList<>();

            for (String posStr : positions) {
                try {
                    int position = Integer.parseInt(posStr);

                    if (position >= 0 && position < hand.size()) {
                        validPositions.add(position);
                    } else {
                        System.out.println("Invalid position: " + position + ". Ignoring.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input: " + posStr + ". Ignoring.");
                }
            }

            validPositions.sort(Collections.reverseOrder());

            for (int pos : validPositions) {
                Card discarded = hand.remove(pos);
                discardedCards.add(discarded);
                System.out.println("Discarded: " + discarded + "\n");
            }

            if (hand.size() > 12) {
                System.out.println("You still need to discard " + (hand.size() - 12) + " more card(s).");
            }
        }
        return discardedCards;
    }

    public boolean wantsToSponsor(QuestCard questCard) {
        System.out.println("Your hand: " + hand);
        System.out.println(name + ", do you want to sponsor the quest: " + questCard + "? (y/n)");


        String response = getInput();
        if (response.equalsIgnoreCase("y")) {
            return true;
        }
        else if (response.equalsIgnoreCase("n")) {
            return false;
        }
        else {
            throw new IllegalArgumentException("Invalid input");
        }
    }

    public boolean canSponsorQuest(QuestCard questCard) {
        int numberOfStages = questCard.getStages();
        List<FoeCard> availableFoes = new ArrayList<>();
        List<WeaponCard> availableWeapons = new ArrayList<>();

        for (Card card : hand) {
            if (card instanceof FoeCard) {
                availableFoes.add((FoeCard) card);
            } else if (card instanceof WeaponCard) {
                availableWeapons.add((WeaponCard) card);
            }
        }

        if (availableFoes.size() < numberOfStages) {
            return false;
        }

        availableFoes.sort(Comparator.comparingInt(FoeCard::getValue));

        Set<Card> usedCards = new HashSet<>();

        int previousStageValue = -1;

        for (int stageIndex = 0; stageIndex < numberOfStages; stageIndex++) {
            FoeCard foeForStage = null;
            for (FoeCard foe : availableFoes) {
                if (!usedCards.contains(foe)) {
                    foeForStage = foe;
                    break;
                }
            }
            if (foeForStage == null) {
                return false;
            }

            usedCards.add(foeForStage);

            int currentStageValue = foeForStage.getValue();

            Set<WeaponCard> weaponsForStage = new HashSet<>();
            Set<String> weaponNamesUsedInStage = new HashSet<>();

            while (currentStageValue <= previousStageValue) {
                boolean weaponAdded = false;
                for (WeaponCard weapon : availableWeapons) {
                    if (!usedCards.contains(weapon) && !weaponNamesUsedInStage.contains(weapon.getName())) {
                        weaponsForStage.add(weapon);
                        weaponNamesUsedInStage.add(weapon.getName());
                        usedCards.add(weapon);
                        currentStageValue += weapon.getValue();
                        weaponAdded = true;
                        break;
                    }
                }
                if (!weaponAdded) {
                    return false;
                }
            }

            if (currentStageValue <= previousStageValue) {
                return false;
            }

            previousStageValue = currentStageValue;
        }

        return true;
    }

    public void sponsorQuest(Quest quest) {
        for (int i = 0; i < quest.getStages().size(); i++) {
            QuestStage stage = quest.getStage(i);
            setupStage(quest, stage, i);
        }
    }

    private void setupStage(Quest quest, QuestStage stage, int stageNumber) {
        System.out.println("\nSetting up stage " + (stageNumber + 1));
        boolean hasFoe = false;

        while(!hand.isEmpty()) {
            System.out.println("\n" + name + "'s hand: " + hand);
            System.out.println("Current stage: " + (stageNumber+1) + "/" + quest.getStages().size());
            System.out.println("Current stage value: " + stage.getValue());
            System.out.println("Enter the position of the card to add to the stage, or 'q' to finish:");

            String input = getInput();

            if (input.equalsIgnoreCase("q")) {
                if (!hasFoe) {
                    System.out.println("The stage must have exactly one Foe card.");
                } else if (stage.getCards().isEmpty()) {
                    System.out.println("A stage cannot be empty. Add at least one card.");
                } else if (stageNumber > 0 && stage.getValue() <= quest.getStages().get(stageNumber - 1).getValue()) {
                    System.out.println("This stage must have a higher value than the previous stage.");
                } else {
                    break;
                }
            }
            else {
                try {
                    int position = Integer.parseInt(input);
                    if (position >= 0 && position < hand.size()) {
                        Card card = hand.get(position);
                        if (card instanceof FoeCard && !hasFoe) {
                            stage.addCard(hand.remove(position));
                            hasFoe = true;
                        } else if (card instanceof WeaponCard && stage.canAddCard(card)) {
                            stage.addCard(hand.remove(position));
                        } else {
                            System.out.println("Invalid card for this stage. Try again.");
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Try again.");
                }
            }
        }
    }

    public boolean wantsToParticipate(QuestStage stage) {
        System.out.println("\n" + name + ", do you want to participate in this quest stage? (y/n)");
        System.out.println("Your hand: " + hand);

        return getInput().trim().equalsIgnoreCase("y");
    }

    public List<Card> buildAttack(QuestStage stage) {
        List<Card> attack = new ArrayList<>();

        while (true) {
            System.out.println("\n" + name + "'s hand: " + hand);
            System.out.println("Current attack: " + attack);
            System.out.println("Current attack value: " + calculateAttackValue(attack));
            System.out.println("Enter the position of the card to add to your attack, or 'q' to finish:");

            String input = getInput();

            if (input.equalsIgnoreCase("q")) {
                break;
            } else {
                try {
                    int position = Integer.parseInt(input);
                    if (position >= 0 && position < hand.size()) {
                        Card card = hand.get(position);
                        if (card instanceof WeaponCard && !attack.contains(card)) {
                            attack.add(hand.remove(position));
                        } else {
                            System.out.println("Invalid card for attack. Try again.");
                        }
                    } else {
                        System.out.println("Invalid position. Try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Try again.");
                }
            }
        }
        return attack;
    }

    private int calculateAttackValue(List<Card> attack) {
        return attack.stream().mapToInt(Card::getValue).sum();
    }
}