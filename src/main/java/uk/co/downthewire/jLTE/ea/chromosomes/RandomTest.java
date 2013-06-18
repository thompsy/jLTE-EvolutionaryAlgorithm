package uk.co.downthewire.jLTE.ea.chromosomes;

import java.util.Random;

public class RandomTest {
	public static void main(String[] args) {

		Random random1 = new Random(1);
		Random random2 = new Random(1);

		for (int i = 0; i < 10; i++) {
			System.out.println("-----");
			System.out.println(random1.nextDouble());
			System.out.println(random2.nextDouble());
		}
	}
}
