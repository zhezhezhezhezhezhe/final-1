import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class MyProgram {
    private static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        printIntro();
        BlackjackGame game = new BlackjackGame(chooseStartingBankroll());
        boolean running = true;

        while (running) {
            printMenu(game.getBankroll());
            int choice = readInt("Choose an option: ");
            System.out.println();

            if (choice == 1) {
                game.playRound(input);
            } else if (choice == 2) {
                printRules();
            } else if (choice == 3) {
                BlackjackTester.runTests();
            } else if (choice == 4) {
                running = false;
                System.out.println("Thanks for playing Blackjack Lite.");
            } else {
                System.out.println("Please enter a number from 1 to 4.");
            }
            System.out.println();
        }
    }

    private static void printIntro() {
        System.out.println("====================================");
        System.out.println("           Blackjack Lite           ");
        System.out.println("====================================");
        System.out.println("Try to get closer to 21 than the dealer");
        System.out.println("without going over.");
        System.out.println();
    }

    private static double chooseStartingBankroll() {
        System.out.println("Choose a difficulty:");
        System.out.println("1. Easy ($150.00)");
        System.out.println("2. Normal ($100.00)");
        System.out.println("3. Hard ($50.00)");
        int choice = readInt("Choose difficulty: ");

        while (choice < 1 || choice > 3) {
            System.out.println("Please enter 1, 2, or 3.");
            System.out.println();
            choice = readInt("Choose difficulty: ");
        }
        System.out.println();

        if (choice == 1) {
            return 150.00;
        } else if (choice == 3) {
            return 50.00;
        } else {
            return 100.00;
        }
    }

    private static void printMenu(double bankroll) {
        System.out.println("Main Menu");
        System.out.println("Bankroll: $" + formatMoney(bankroll));
        System.out.println("1. Play round");
        System.out.println("2. View rules");
        System.out.println("3. Run tests");
        System.out.println("4. Quit");
        System.out.println();
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!input.hasNextInt()) {
            input.nextLine();
            System.out.print("Enter a whole number: ");
        }
        int value = input.nextInt();
        input.nextLine();
        return value;
    }

    private static void printRules() {
        System.out.println("Rules");
        System.out.println("- Number cards are worth their number.");
        System.out.println("- J, Q, and K are worth 10.");
        System.out.println("- Aces are worth 11 unless that would bust, then they are worth 1.");
        System.out.println("- You can hit to take a card or stay to stop.");
        System.out.println("- The dealer hits until reaching 17 or higher.");
        System.out.println("- Blackjack pays 3 to 2.");
        System.out.println();
    }

    public static String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }
}

class BlackjackGame {
    private Deck deck;
    private double bankroll;
    private int roundsPlayed;
    private int wins;
    private int losses;
    private int pushes;
    private int blackjacks;

    public BlackjackGame(double startingBankroll) {
        deck = new Deck();
        bankroll = startingBankroll;
        roundsPlayed = 0;
        wins = 0;
        losses = 0;
        pushes = 0;
        blackjacks = 0;
    }

    public double getBankroll() {
        return bankroll;
    }

    public void playRound(Scanner input) {
        if (bankroll < 1.0) {
            System.out.println("You need at least $1.00 to place a bet.");
            System.out.println("You can quit from the main menu.");
            System.out.println();
            return;
        }

        if (deck.cardsLeft() < 15) {
            deck = new Deck();
            System.out.println("The deck was reshuffled.");
            System.out.println();
        }

        int bet = askForBet(input);
        Hand player = new Hand();
        Hand dealer = new Hand();

        player.addCard(deck.dealCard());
        dealer.addCard(deck.dealCard());
        player.addCard(deck.dealCard());
        dealer.addCard(deck.dealCard());

        printDealerHiddenHand(dealer);
        printHand("Your hand", player);
        System.out.println();

        if (player.isBlackjack() || dealer.isBlackjack()) {
            finishInstantBlackjack(player, dealer, bet);
            return;
        }

        playPlayerTurn(input, player);
        if (player.isBust()) {
            bankroll -= bet;
            roundsPlayed++;
            losses++;
            printHand("Your final hand", player);
            System.out.println("You busted and lost $" + formatMoney(bet) + ".");
            System.out.println();
            printRoundSummary();
            return;
        }

        playDealerTurn(dealer);
        finishRound(player, dealer, bet);
    }

    private int askForBet(Scanner input) {
        System.out.print("Enter your bet: $");
        int bet = readPositiveInt(input);

        while (bet <= 0 || bet > bankroll) {
            System.out.print("Bet must be between $1 and $" + formatMoney(bankroll) + ": $");
            bet = readPositiveInt(input);
        }
        return bet;
    }

    private int readPositiveInt(Scanner input) {
        while (!input.hasNextInt()) {
            input.nextLine();
            System.out.print("Enter a whole number: ");
        }
        int value = input.nextInt();
        input.nextLine();
        return value;
    }

    private void playPlayerTurn(Scanner input, Hand player) {
        boolean turnOver = false;
        while (!turnOver && !player.isBust()) {
            System.out.print("Hit or stay? Enter h or s: ");
            String answer = input.nextLine().trim().toLowerCase();

            if (answer.equals("h") || answer.equals("hit")) {
                Card card = deck.dealCard();
                player.addCard(card);
                System.out.println("You drew: " + card);
                printHand("Your hand", player);
                System.out.println();
            } else if (answer.equals("s") || answer.equals("stay")) {
                turnOver = true;
                System.out.println("You stayed.");
                System.out.println();
            } else {
                System.out.println("Please enter h or s.");
                System.out.println();
            }
        }
    }

    private void playDealerTurn(Hand dealer) {
        System.out.println();
        System.out.println("Dealer reveals the hidden card.");
        printHand("Dealer hand", dealer);

        while (dealer.getValue() < 17) {
            Card card = deck.dealCard();
            dealer.addCard(card);
            System.out.println("Dealer drew: " + card);
            printHand("Dealer hand", dealer);
            System.out.println();
        }
    }

    private void finishInstantBlackjack(Hand player, Hand dealer, int bet) {
        printHand("Your final hand", player);
        printHand("Dealer final hand", dealer);
        System.out.println();
        roundsPlayed++;

        if (player.isBlackjack() && dealer.isBlackjack()) {
            pushes++;
            blackjacks++;
            System.out.println("Both sides have blackjack. Push.");
        } else if (player.isBlackjack()) {
            double winnings = bet * 1.5;
            bankroll += winnings;
            wins++;
            blackjacks++;
            System.out.println("Blackjack! You won $" + formatMoney(winnings) + ".");
        } else {
            bankroll -= bet;
            losses++;
            System.out.println("Dealer has blackjack. You lost $" + formatMoney(bet) + ".");
        }
        System.out.println();
        printRoundSummary();
    }

    private void finishRound(Hand player, Hand dealer, int bet) {
        int playerValue = player.getValue();
        int dealerValue = dealer.getValue();

        printHand("Your final hand", player);
        printHand("Dealer final hand", dealer);
        System.out.println();

        if (dealer.isBust()) {
            bankroll += bet;
            roundsPlayed++;
            wins++;
            System.out.println("Dealer busted. You won $" + formatMoney(bet) + ".");
        } else if (playerValue > dealerValue) {
            bankroll += bet;
            roundsPlayed++;
            wins++;
            System.out.println("You won $" + formatMoney(bet) + ".");
        } else if (playerValue < dealerValue) {
            bankroll -= bet;
            roundsPlayed++;
            losses++;
            System.out.println("You lost $" + formatMoney(bet) + ".");
        } else {
            roundsPlayed++;
            pushes++;
            System.out.println("Push. Your bet was returned.");
        }
        System.out.println();
        printRoundSummary();
    }

    private void printDealerHiddenHand(Hand dealer) {
        System.out.println("Dealer hand: " + dealer.getCard(0) + ", [Hidden]");
    }

    private void printHand(String label, Hand hand) {
        System.out.println(label + ": " + hand + " Value: " + hand.getValue());
    }

    private void printRoundSummary() {
        System.out.println("Current bankroll: $" + formatMoney(bankroll));
        System.out.println("Stats: " + roundsPlayed + " rounds, " + wins + " wins, "
                + losses + " losses, " + pushes + " pushes, "
                + blackjacks + " blackjacks");
        System.out.println();
    }

    private String formatMoney(double amount) {
        return MyProgram.formatMoney(amount);
    }
}

class Hand {
    private ArrayList<Card> cards;

    public Hand() {
        cards = new ArrayList<Card>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public int size() {
        return cards.size();
    }

    public int getValue() {
        int total = 0;
        int aces = 0;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            total += card.getBlackjackValue();
            if (card.getRank().equals("A")) {
                aces++;
            }
        }

        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getValue() == 21;
    }

    public boolean isBust() {
        return getValue() > 21;
    }

    public String toString() {
        String result = "";
        for (int i = 0; i < cards.size(); i++) {
            result += cards.get(i);
            if (i < cards.size() - 1) {
                result += ", ";
            }
        }
        return result;
    }
}

class Deck {
    private ArrayList<Card> cards;
    private Random random;

    public Deck() {
        cards = new ArrayList<Card>();
        random = new Random();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7",
                "8", "9", "10", "J", "Q", "K"};

        for (int s = 0; s < suits.length; s++) {
            for (int r = 0; r < ranks.length; r++) {
                cards.add(new Card(ranks[r], suits[s]));
            }
        }
        shuffle();
    }

    public void shuffle() {
        for (int i = 0; i < cards.size(); i++) {
            int randomIndex = random.nextInt(cards.size());
            Card temp = cards.get(i);
            cards.set(i, cards.get(randomIndex));
            cards.set(randomIndex, temp);
        }
    }

    public Card dealCard() {
        if (cards.size() == 0) {
            return new Card("A", "Spades");
        }
        return cards.remove(cards.size() - 1);
    }

    public int cardsLeft() {
        return cards.size();
    }
}

class Card {
    private String rank;
    private String suit;

    public Card(String rank, String suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public String getRank() {
        return rank;
    }

    public int getBlackjackValue() {
        if (rank.equals("A")) {
            return 11;
        } else if (rank.equals("K") || rank.equals("Q") || rank.equals("J")) {
            return 10;
        } else {
            return Integer.parseInt(rank);
        }
    }

    public String toString() {
        return rank + " of " + suit;
    }
}

class BlackjackTester {
    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void runTests() {
        testsRun = 0;
        testsPassed = 0;
        testCardValues();
        testHandValue();
        testAceCanBecomeOne();
        testMultipleAces();
        testBlackjack();
        testTwentyOneIsNotAlwaysBlackjack();
        testBust();
        testDeckSize();
        System.out.println();
        System.out.println("Tests passed: " + testsPassed + " / " + testsRun);
        System.out.println();
    }

    private static void testCardValues() {
        check("number card value", new Card("7", "Hearts").getBlackjackValue() == 7);
        check("face card value", new Card("K", "Spades").getBlackjackValue() == 10);
        check("ace starts as 11", new Card("A", "Clubs").getBlackjackValue() == 11);
    }

    private static void testHandValue() {
        Hand hand = new Hand();
        hand.addCard(new Card("10", "Hearts"));
        hand.addCard(new Card("7", "Clubs"));
        check("hand value adds cards", hand.getValue() == 17);
    }

    private static void testAceCanBecomeOne() {
        Hand hand = new Hand();
        hand.addCard(new Card("A", "Hearts"));
        hand.addCard(new Card("9", "Clubs"));
        hand.addCard(new Card("8", "Spades"));
        check("ace changes from 11 to 1", hand.getValue() == 18);
    }

    private static void testMultipleAces() {
        Hand hand = new Hand();
        hand.addCard(new Card("A", "Hearts"));
        hand.addCard(new Card("A", "Clubs"));
        hand.addCard(new Card("9", "Spades"));
        check("A + A + 9 equals 21", hand.getValue() == 21);
    }

    private static void testBlackjack() {
        Hand hand = new Hand();
        hand.addCard(new Card("A", "Hearts"));
        hand.addCard(new Card("K", "Clubs"));
        check("blackjack with ace and face card", hand.isBlackjack());
    }

    private static void testTwentyOneIsNotAlwaysBlackjack() {
        Hand hand = new Hand();
        hand.addCard(new Card("7", "Hearts"));
        hand.addCard(new Card("7", "Clubs"));
        hand.addCard(new Card("7", "Spades"));
        check("three-card 21 is not blackjack", !hand.isBlackjack());
    }

    private static void testBust() {
        Hand hand = new Hand();
        hand.addCard(new Card("10", "Hearts"));
        hand.addCard(new Card("9", "Clubs"));
        hand.addCard(new Card("5", "Spades"));
        check("hand over 21 busts", hand.isBust());
    }

    private static void testDeckSize() {
        Deck deck = new Deck();
        check("new deck has 52 cards", deck.cardsLeft() == 52);
        deck.dealCard();
        check("dealing removes one card", deck.cardsLeft() == 51);
    }

    private static void check(String name, boolean passed) {
        testsRun++;
        if (passed) {
            testsPassed++;
            System.out.println("[PASS] " + name);
        } else {
            System.out.println("[FAIL] " + name);
        }
    }
}
