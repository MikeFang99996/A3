package org.example;

import org.example.cards.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Game {
    private List<Player> players;
    private Deck adventureDeck;
    private Deck eventDeck;
    private int currentPlayerIndex;
    private Quest currentQuest;

    public Game() {
        players = new ArrayList<>();
        adventureDeck = new Deck();
        eventDeck = new Deck();
        currentPlayerIndex = 0;
    }

    public Deck         getAdventureDeck()      { return adventureDeck; }
    public Deck         getEventDeck()          { return eventDeck; }
    public List<Player> getPlayers()            { return players; }
    public int          getCurrentPlayerIndex() { return currentPlayerIndex; }
    public Quest        getCurrentQuest()       { return currentQuest; }

    private Map<Player, Queue<String>> playerResponses = new HashMap<>();

    public void setCurrentQuest(Quest quest) {
        currentQuest = quest;
        currentPlayerIndex = quest.getSponsor().getIndex();
    }

    public void setup() {
        setupAdventureDeck();
        setupEventDeck();
        createPlayers();
        distributeInitialCards();
    }

    private void setupAdventureDeck() {
        //foe cards
        int[] foeValues = {5, 10, 15, 20, 25, 30, 35, 40, 50, 70};
        int[] foeCounts = {8, 7, 8, 7, 7, 4, 4, 2, 2, 1};
        for (int i = 0; i < foeValues.length; i++) {
            for (int j = 0; j < foeCounts[i]; j++) {
                adventureDeck.addCard(new FoeCard(foeValues[i]));
            }
        }
        //weapon cards
        char[] types = {'D', 'H', 'S', 'B', 'L', 'E'};
        int[] values = {5, 10, 10, 15, 20, 30};
        int[] counts = {6, 12, 16, 8, 6, 2};
        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < counts[i]; j++) {
                adventureDeck.addCard(new WeaponCard(types[i], values[i]));
            }
        }

        adventureDeck.shuffle();
    }

    private void setupEventDeck() {
        for (int i = 0; i < 3; i++) eventDeck.addCard(new QuestCard(2));
        for (int i = 0; i < 4; i++) eventDeck.addCard(new QuestCard(3));
        for (int i = 0; i < 3; i++) eventDeck.addCard(new QuestCard(4));
        for (int i = 0; i < 2; i++) eventDeck.addCard(new QuestCard(5));

        eventDeck.addCard(new EventCard(EventType.PLAGUE));
        for (int i = 0; i < 2; i++) eventDeck.addCard(new EventCard(EventType.QUEENS_FAVOR));
        for (int i = 0; i < 2; i++) eventDeck.addCard(new EventCard(EventType.PROSPERITY));

        eventDeck.shuffle();
    }

    private void createPlayers() {
        for (int i = 0; i < 4; i++) {
            players.add(new Player("P" + (i + 1)));
        }
    }

    private void distributeInitialCards() {
        for (Player player : players) {
            for (int i = 0; i < 12; i++) {
                player.addCard(adventureDeck.drawCard());
                player.trimHand();
            }
        }
    }

    public boolean isGameOver() {
        return players.stream().anyMatch(player -> player.getShields() >= 7);
    }

    private void displayPlayerHand(Player player) {
        player.sortHand();
        System.out.println(player.getName() + "'s hand: " + player.getHand());
    }

    public void handleEventCard(Card eventCard, Player currentPlayer) {
        if (eventCard instanceof EventCard event) {
            switch (event.getEventType()) {
                case PLAGUE:
                    currentPlayer.removeShields(2);
                    System.out.println(currentPlayer.getName() + " loses 2 shields due to Plague.\n");
                    break;
                case QUEENS_FAVOR:
                    for (int i = 0; i < 2; i++) {
                        currentPlayer.addCard(adventureDeck.drawCard());
                    }
                    adventureDeck.addDiscards(currentPlayer.trimHand());
                    System.out.println(currentPlayer.getName() + " draws 2 adventure cards due to Queen's Favor.\n");
                    break;
                case PROSPERITY:
                    for (Player player : players) {
                        for (int i = 0; i < 2; i++) {
                            player.addCard(adventureDeck.drawCard());
                        }
                        adventureDeck.addDiscards(player.trimHand());
                    }
                    System.out.println("All players draw 2 adventure cards due to Prosperity.\n");
                    break;
            }
        } else if (eventCard instanceof QuestCard) {
            handleQuestCard((QuestCard) eventCard, currentPlayer);
        }

        eventDeck.addDiscard(eventCard);
    }

    private void handleQuestCard(QuestCard questCard, Player currentPlayer) {
        System.out.println("A quest card has been drawn: " + questCard + "\n");
        Player sponsor = findSponsor(questCard);
        if (sponsor != null) {
            System.out.println(sponsor.getName() + " decides to become the sponsor of the quest.\n");
            currentQuest = new Quest(questCard, sponsor);
            sponsor.sponsorQuest(currentQuest);
            resolveQuest();
        }
        else {
            System.out.println("No sponsor found, quest discarded.\n");
        }
    }

    public void resolveQuest() {
        List<Player> participants = new ArrayList<>(players);
        participants.remove(currentQuest.getSponsor());

        for (int stageIndex = 0; stageIndex < currentQuest.getStages().size(); stageIndex++) {
            QuestStage stage = currentQuest.getStages().get(stageIndex);
            System.out.println("\nResolving stage " + (stageIndex + 1) + ": ");

            List<Player> stageParticipants = new ArrayList<>();
            for (Player player : participants) {
                if (player.wantsToParticipate(stage)) {
                    stageParticipants.add(player);
                    System.out.println("draw " + adventureDeck.getCards().get(adventureDeck.getCards().size() - 1));
                    player.addCard(adventureDeck.drawCard());
                    player.trimHand();
                }
            }

            if (stageParticipants.isEmpty()) {
                System.out.println("No participants for this stage. Quest ends.");
                break;
            }

            List<Player> successfulParticipants = new ArrayList<>();

            for (Player player : stageParticipants) {
                List<Card> attack = player.buildAttack(stage);
                int attackValue = calculateAttackValue(attack);
                if (attackValue >= stage.getValue()) {
                    System.out.println(player.getName() + " succeeded in the stage.");
                    successfulParticipants.add(player);
                } else {
                    System.out.println(player.getName() + " failed the stage.");
                }
                player.discardCards(attack);
                adventureDeck.addDiscards(attack);
            }
            participants = successfulParticipants;

            if (participants.isEmpty()) {
                System.out.println("No participants succeeded. Quest ends.");
                break;
            }

            if (stageIndex == currentQuest.getStages().size() - 1) {
                for (Player winner : participants) {
                    winner.addShields(currentQuest.getStages().size());
                    System.out.println(winner.getName() + " completed the quest and gained " + currentQuest.getStages().size() + " shield(s)!\n");
                }
            }
        }
        handleSponsorDiscard(currentQuest);
        handleSponsorCardDrawing();
        currentQuest = null;
    }

    public void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        System.out.println("It's " + currentPlayer.getName() + "'s turn.");
        displayPlayerHand(currentPlayer);

        Card eventCard = eventDeck.drawCard();

        if (eventCard != null) {
            System.out.println(currentPlayer.getName() + " drew: " + eventCard);
            handleEventCard(eventCard, currentPlayer);
        } else {
            System.out.println("No more event cards available. Game ends.");
            return;
        }

        System.out.println(currentPlayer.getName() + "'s turn has ended.\n");

        clearDisplay();

        if (!isGameOver()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    private void clearDisplay() {
        for (int i = 0; i < 10; i++) {
            System.out.println();
        }
    }

    public Player findSponsor(QuestCard questCard) {
        int startIndex = currentPlayerIndex;
        for (int i = 0; i < players.size(); i++) {
            int index = (startIndex + i) % players.size();
            Player player = players.get(index);
            if (player.wantsToSponsor(questCard)) {
                if(player.canSponsorQuest(questCard)) {
                    return player;
                }
                else {
                    System.out.println("Your hand cannot support this quest.\n");
                }
            }
        }
        return null;
    }

    public int calculateAttackValue(List<Card> attack) {
        return attack.stream().mapToInt(Card::getValue).sum();
    }

    private void handleSponsorDiscard(Quest quest) {
        for (QuestStage stage : quest.getStages()) {
            adventureDeck.addDiscards(new ArrayList<>(stage.getCards()));
            stage.getCards().clear();
        }
    }

    private void handleSponsorCardDrawing() {
        Player sponsor = currentQuest.getSponsor();
        int cardsUsed = currentQuest.getStages().stream().mapToInt(stage -> stage.getCards().size()).sum();
        for (int i = 0; i < cardsUsed + currentQuest.getStages().size(); i++) {
            sponsor.addCard(adventureDeck.drawCard());
        }
        sponsor.trimHand();
    }

    public void playGame() {
        setup();
        while (!isGameOver()) {
            playTurn();
        }
        displayWinners();
    }

    private void displayWinners() {
        List<Player> winners = players.stream().filter(player -> player.getShields() >= 7).collect(Collectors.toList());
        System.out.println("Game Over! Winner(s):");
        for (Player winner : winners) {
            System.out.println(winner.getName() + " with " + winner.getShields() + " shields");
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.playGame();
    }
}
