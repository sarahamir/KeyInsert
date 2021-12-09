package KITmain;
import java.util.Random;

import Analysis.ReadWrite;


public class RandomKeyGenerator {

	public String key = "";

	/**
	 * Author: Sarah Amir
	 */
	public RandomKeyGenerator (int keyLength, String outputKeyFileName){
//		String key = "";
		Random randomGenerator = new Random();
		for (int i = 0; i < keyLength; i++) {
			int random = randomGenerator.nextInt(2);
			key += random;
		}
		System.out.println(key);
		ReadWrite rw = new ReadWrite();
		rw.fileWriter(key, outputKeyFileName);		
	}
	
	public RandomKeyGenerator (int keyLength){
//		String key = "";
		Random randomGenerator = new Random();
		for (int i = 0; i < keyLength; i++) {
			int random = randomGenerator.nextInt(2);
			key += random;
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 1){
			System.out.println("Usage : java RansdomKeyGenerator <lengthOfKey> <outputKeyFileName>");
			System.out.println("e.g. : $ java RansdomKeyGenerator 25 key.txt");
			// System.out.println("Key length is not limited");
			return;
		}
		
		int keyLength = Integer.parseInt(args[0]);
		String outputFileName = args[1];
		
		RandomKeyGenerator newKey = new RandomKeyGenerator(keyLength, outputFileName);
		
		System.out.println("Key file named " + outputFileName +  " is created.");
	}

}
