import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Card {
	public int value;
	public String suit;
	public String name;

	public static ArrayList<Card> setDeck() {
		String[] suits = { "Clubs", "Diamonds", "Hearts", "Spades" };
		String[] names = { "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen",
				"King", "Ace" };

		ArrayList<Card> deck = new ArrayList<Card>();
		int tmp = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 13; j++) {
				Card c = new Card();
				c.suit = suits[i];
				c.name = names[j];

				if (j >= 9) {
					c.value = 10;
				} else {
					c.value = j + 2;
				}
				if (c.name == "Ace") {
					c.value = 11;
				}
				deck.add(c);
				tmp++;
			}
		}
		long seed = System.nanoTime();
		Collections.shuffle(deck, new Random(seed));
		return deck;
	}

	public static void main(String[] args) {

		for (int i = 0; i <= 4; i++) {
			Card card = new Card();
			ArrayList<Card> deck = Card.setDeck();

			card = deck.get(0);

			System.out.println(card.name);
			System.out.println(card.suit);
			System.out.println(card.value + "\n");
		}
	}

}
