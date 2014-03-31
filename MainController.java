/**
 * The logic of the decryption program is based on a "hill-climber" logic. The flow of the program is as follows:
 * 1. The program generates a possible KEY by matching the frequency of letters.
 * 2. The program decrypt the text with the possible KEY. 
 * 3. The program then generate a new KEY by changing one of the element in the possible KEY. 
 * 4. The program then decrypts the encrypted text with the newly generated KEY. 
 * 5. The program then compares these two decrypted text and scores them accordingly. (See Scoring Algorithm)
 * 6. The decrypted text with the higher score will be retained, as well as the associated KEY. 
 * 7. The associated KEY will now be the possible KEY. (Since it is a more possible/likely KEY than the other)
 * 8. The KEY of the lower score decrypted text will be rejected and discarded.
 * 9. Repeat step 3-8.
 * 
 * The Decryption Algorithm consists of two main algorithm: key generator Algorithm and scoring Algorithm
 * 
 * Key Generator ALGORITHM:
 * It consists of two loops, choosing two alphabets in a key.
 * The swapKEY(char, char) function then swaps the two chosen alphabets and generates a new KEY
 * 
 * PSEUDO CODE FOR KEY GENERATOR ALGORITHM:
 * FOR i = 1 step to 25 {
 * 	FOR j = 0 step to 24 {
 * 		swap keys at index j with index (j+i)
 * 		//Scoring Algorithm Operation goes here
 * 	}
 * }
 * 
 * Scoring ALGORITHM:
 * The Scoring Algorithm takes two decrypted text that was decrypted using different KEYs and scores them accordingly.
 * It scores the decrypted text based on frequency of 2-grams, 3-grams and 4-grams.
 * 
 * PSEUDO CODE FOR SCORING ALGORITHM:
 * IF(decryptedBuffer1's N-gram frequency > decryptedBuffer2's N-gram frequency)
 * 		decryptedBuffer1's score++
 * ELSEIF(decryptedBuffer1's N-gram frequency < decryptedBuffer2's N-gram frequency)
 * 		decryptedBuffer2's score++
 * ELSE
 * 		do nothing when frequency are equal
 * 
 * Cycle Count: ONE CYCLE is counted when the outer loop of the Key Generator Algorithm completes.
 * For the first 3 cycles, the Scoring Algorithm will use a small frequency list size of 30, to improve execution time.
 * With a small frequency list size, it will execute faster and it will still be able to decipher most of the common letters.
 * Subsequently after 3 cycles, the frequency list size will increment by 300, to decrypt tougher letters (especially letters with low frequency).
 * By dynamically increasing the frequency list size, it will avoid using the full list of frequency which can be huge and slow to process
 *  (trigram frequency list contain 17,576 entries).
 * 
 * 
 * @author SEET YONG SONG, KENNY
 * @author TAY JIN HENG
 * @author YEOH KENG WEI
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

public class MainController {
	
	private final static char[] alphabets = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	private static char[] key = new char[26];	//Char array to store key
	
	//Variables to store mapping KEY
	private static HashMap<Character, Character> mappingKEY = new HashMap<Character, Character>();	//HashMap<Plaintext, Ciphertext>
	private static ArrayList<Character> mappingKEYArray = new ArrayList<Character>();

	private static final int sampleSize = 2000;										//Sample size of the text to be used for decryption algorithm
	private static char[] decryptedBuffer1 = new char[sampleSize];			//To store the decrypted text of the 'more likely' KEY
	private static char[] decryptedBuffer2 = new char[sampleSize];		//To store the decrypted text of the challenging KEY
	
	private static String[] commonLetters = {"e","t","a","o","i","n","s","r","h","d","l","u","c","m","f","y","w","g","p","b","v","k","x","q","j","z"};
	
	private static String[] commonQuadrigrams = {"that","ther","with","tion","here","ould","ight","have","hich","whic","this","thin","they","atio",
											"ever","from","ough","were","hing","ment", "quite"};	
	
	//convert String array to arrayList
	private static List<String> commonLettersArray = Arrays.asList(commonLetters);
	private static List<String> commonQuadrigramsArray = Arrays.asList(commonQuadrigrams);
	
	//instantiate arrayList to store data from frequency list
	private static List<String> commonDigramsArray = new ArrayList<String>();
	private static List<String> commonTrigramsArray = new ArrayList<String>();
	private static List<String> digramsArray = new ArrayList<String>();
	private static List<String> trigramsArray = new ArrayList<String>();
	
	public static void main(String[] args) throws IOException {
		
		//loading frequency file into arrayList
	    loadFrequencyFile("digramFrequency.txt", commonDigramsArray, 30);	//load first 30 common Digrams
	    loadFrequencyFile("trigramFrequency.txt", commonTrigramsArray, 30);	//load first 30 common Trigrams
		loadFrequencyFile("digramFrequency.txt", digramsArray, 0);			//load entire list of Digrams
		loadFrequencyFile("trigramFrequency.txt", trigramsArray, 0);		//load entire list of Trigrams
		
		//Menu
		System.out.println("Please enter the encrypted text file name (Including the file type (.txt): ");
		Scanner sc = new Scanner(System.in);
		String fileName = sc.next();

		//Load the ciphertext file and store it in Cryptogram Object
		Cryptogram ciphertext = new Cryptogram(fileName);				
	
		//Generate a possible KEY, by mapping the letters by frequency of both English letters and ciphertext letters
		int index = 0;
		for(Entry<Character, Float> entry : ciphertext.getFrequency().entrySet()) {
			mappingKEY.put(commonLettersArray.get(index).charAt(0), entry.getKey());
			mappingKEYArray.add(entry.getKey());
			index++;
		}
		
		//Print initial mapping KEY used
		System.out.println("\n Initial Mapping KEY: ");
		System.out.println(mappingKEY.toString());

		//Decrypt the ciphertext and store it in a buffer
		decryptedBuffer1 = ciphertext.decrypt(mappingKEY, sampleSize); 

		//Initialize necessary variables, counters and flags
		int cyclesCount = 0;							//Counter to track the number of cycles initiated
		boolean changeFlag = false; 					//Flag to indicate if there is a change of KEY in a cycle
		int noChangeCount = 0;							//Counter to track the number of cycles that have no change in KEY
		int frequencyListSize = 30;						//Size of the frequency list to use, initially 30, will slowly increment
		long startTime = System.currentTimeMillis();	//Variable for calculating the time taken to decrypt text
		
		//Decryption algorithm starts
		do{	
			System.out.println("\n Decrypting... Please wait...");

			//Key Generator Algorithm
			for(int increment=1; increment<26; increment++) {
				for(int k=0; k<25; k++) {
					
					//Check for out of index exception
					if(k+increment > 25) {
						break;
					}

					//Swap two letters in the KEY, generating a new KEY
					char key1 = mappingKEYArray.get(k);
					char key2 = mappingKEYArray.get(k+increment);
					swapKEY(key1, key2);

					//Decrypt the ciphertext with the new KEY and store it in a second buffer
					decryptedBuffer2 = ciphertext.decrypt(mappingKEY, sampleSize);

					//Scoring Algorithm
					if(scoringAlgorithm(decryptedBuffer1, decryptedBuffer2, frequencyListSize) == 1) {
						//decryptedBuffer1 has higher score than decryptedBuffer2, use previous KEY
						swapKEY(key1, key2);
					}
					else {
						//decryptedBuffer2 has higher score than decryptedBuffer1, use current KEY
						decryptedBuffer1 = decryptedBuffer2;
						
						//Prints the new decrypted text, when there is a change
						System.out.println("\nDecrypted Text:");
						System.out.println(decryptedBuffer1);
						System.out.println("Decrypting... Please wait...");
						changeFlag = true;	//Set changeFlag to True
					}

				}
			}

			//End of cycle
			
			//Increment the cycle counter at the end of each cycle
			cyclesCount++;	

			if(changeFlag==false) {
				noChangeCount++;
			}

			//If there is a change in the KEY, reset the counters
			else {
				noChangeCount = 0;
				changeFlag = false;
				
				//Increment the size of frequencyList only after the 3rd cycle
				//This is to maintain low frequency list size in the initial stages, to improve the execution time
				if(cyclesCount>3) {
					frequencyListSize+= 300;	
				}
			}
			
			//If there was no change for 2 entire cycles, stop the decryption algorithm
			if(noChangeCount == 2) {
				break;
			}
		}while(true);	//Keeps running the decryption algorithm, till the break condition.
	
		System.out.println("\nDecryption Completed!");
		//Calculate and prints the total time taken to decrypt
		long endTime = System.currentTimeMillis();
		System.out.println("Took: " + (endTime-startTime) + "ms");
		
		//Updating the key[] array with the mappingKEY used
		for(int i=0; i<alphabets.length; i++) {
			key[i] = mappingKEY.get(alphabets[i]);
		}
		
		//Print out the mappingKEY used, 
		System.out.println("\nKEY Mapping: ");
		System.out.println(mappingKEY.toString());
		System.out.println("Plaintext: " + new String(alphabets));
		System.out.println("KEY: " + new String(key));
		
		
		//End of decryption algorithm, shows menu for further actions from user
		while(true) {
			//Menu
			System.out.println("\n===============================================");
			System.out.println("Please select an option (1-5): ");
			System.out.println("1. Print the decrypted text");
			System.out.println("2. Print the original ciphertext");
			System.out.println("3. Manually swap the KEYS");
			System.out.println("4. View KEY");
			System.out.println("5. Export the decrypted plaintext into a .txt file");
			System.out.println("6. Exit the program");
			System.out.println("===============================================");

			//User's input
			System.out.print("Option: ");
			int choice = sc.nextInt();
			
			switch(choice){
			case 1:
				//prints the ENTIRE decrypted text with the current KEY
				System.out.println(ciphertext.decrypt(mappingKEY,0));
				break;
				
			case 2:
				//Prints out the original ciphertext
				System.out.println(ciphertext.getText());
				break;			
			
			case 3: {
				//Manually swap the keys of the two given plaintext alphabet
				System.out.println("\nPlaintext: " + new String(alphabets));
				System.out.println("KEY: " + new String(key));
				
				System.out.println("\nSelect the two PLAINTEXT alphabet you wish to swap: ");
				String input;
				
				//Keep prompting user to input, if the input is invalid
				do {
					System.out.print("First alphabet (a-z OR A-Z): ");
					input = sc.next();
				}while((input.length()!=1)||!isLatinLetter((input.charAt(0))));
				char key1 = input.charAt(0);
				
				//Keep prompting user to input, if the input is invalid
				do{
					System.out.print("Second alphabet (a-z OR A-Z): ");
					input = sc.next();	
				}while((input.length()!=1)||!isLatinLetter((input.charAt(0))));
				char key2 = input.charAt(0);
				
				//Swap the two KEYs associated to the given alphabets 
				swapKEY(mappingKEY.get(key1), mappingKEY.get(key2));
				
				//Update the key array
				for(int i=0; i<alphabets.length; i++) {
					key[i] = mappingKEY.get(alphabets[i]);
				}
				System.out.println("Swapping done!");
				break;
			}
			case 4: {
				//Prints out the key
				System.out.println("\nKEY Mapping: ");
				System.out.println(mappingKEY.toString());
				System.out.println("Plaintext: " + new String(alphabets));
				System.out.println("KEY: " + new String(key));
				break;
			}
			case 5: {
				//Output a .txt file
				BufferedWriter writer = null;
				File outputFile = null;
				try {
					outputFile = new File("plaintext.txt");
					
					writer = new BufferedWriter(new FileWriter(outputFile));
					writer.write(ciphertext.decrypt(mappingKEY, 0));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						writer.close();
					} catch (Exception e) {
						
					}
				}
				System.out.println("File Exported!");
				System.out.println("File path: " + outputFile.getCanonicalPath());
				break;
			}
			case 6: {
				
				//Terminates program
				System.out.println("Terminating Program...");
				System.exit(0);
				break;
			}
			default: 
				System.out.println("Invalid Choice.");
				break;
			}
		}
	}
	
	/**
	 * This scoring algorithm compares the two given decrypted buffer and scores them accordingly.
	 * returns 1 if decryptedBuffer1 has more score
	 * returns 2 if decryptedBuffer2 has more score
	 * uses a variable list size of digrams and trigrams, size is defined by parameter
	 * 
	 * 
	 * @param decryptedBuffer1	the char array containing the decrypted text using first KEY
	 * @param decryptedBuffer2	the char array containing the decrypted text using second KEY
	 * @param size				an integer to determine the size of the frequency list to use
	 * @return 1				if decryptedBuffer1 has more score 
	 * @return 2				if decryptedBuffer2 has more score or equivalent to decryptedBuffer1
	 */
	public static int scoringAlgorithm(char[] decryptedBuffer1, char[] decryptedBuffer2, int size) {
		int score1 = 0;
		int score2 = 0;
		int digramSize = size;
		int trigramSize = size;
		
		//calculate the score based on digram
		if(size>digramsArray.size()) {
			digramSize = digramsArray.size();
		}
		
		for(int i=0; i<sampleSize-1; i++) {
			String s1 ="" + decryptedBuffer1[i] + decryptedBuffer1[i+1];
			String s2 ="" + decryptedBuffer2[i] + decryptedBuffer2[i+1];
			
			if(digramsArray.subList(0, digramSize).contains(s1) && digramsArray.subList(0, digramSize).contains(s2)) {
				if(digramsArray.subList(0, digramSize).indexOf(s1) < digramsArray.indexOf(s2)) {
					score1++;
				}
				else if(digramsArray.subList(0, digramSize).indexOf(s1) > digramsArray.indexOf(s2)) {
					score2++;
				}
			} else if(digramsArray.subList(0, digramSize).contains(s1)) {
				score1++;
			} else if (digramsArray.subList(0, digramSize).contains(s2)) {
				score2++;
			} 
		}
		
		//calculate the score based on Trigram
		if(size>trigramsArray.size()) {
			trigramSize = trigramsArray.size();
		}
		
		for(int i=0; i<sampleSize-2; i++) {
			String s1 ="" + decryptedBuffer1[i] + decryptedBuffer1[i+1] + decryptedBuffer1[i+2];
			String s2 ="" + decryptedBuffer2[i] + decryptedBuffer2[i+1] + decryptedBuffer2[i+2];
			
			if(trigramsArray.subList(0, trigramSize).contains(s1) && trigramsArray.subList(0, trigramSize).contains(s2)) {
				if(trigramsArray.subList(0, trigramSize).indexOf(s1) < trigramsArray.indexOf(s2)) {
					score1++;
				}
				else if(trigramsArray.subList(0, trigramSize).indexOf(s1) > trigramsArray.indexOf(s2)){
					score2++;
				}
			} else if(trigramsArray.subList(0, trigramSize).contains(s1)) {
				score1++;
			} else if(trigramsArray.subList(0, trigramSize).contains(s2)) {
				score2++;
			}
		}
		
		//calculate the score based on Quadrigram
		for(int i=0; i<sampleSize-3; i++) {
			String s1 ="" + decryptedBuffer1[i] + decryptedBuffer1[i+1] + decryptedBuffer1[i+2] + decryptedBuffer1[i+3];
			String s2 ="" + decryptedBuffer2[i] + decryptedBuffer2[i+1] + decryptedBuffer2[i+2] + decryptedBuffer2[i+3];
			
			if(commonQuadrigramsArray.contains(s1) && commonQuadrigramsArray.contains(s2)) {
				if(commonQuadrigramsArray.indexOf(s1) < commonQuadrigramsArray.indexOf(s2)) {
					score1++;
				}
				else if(commonQuadrigramsArray.indexOf(s1) > commonQuadrigramsArray.indexOf(s2)) {
					score2++;
				}
			} else if(commonQuadrigramsArray.contains(s1)) {
				score1++;
			} else if(commonQuadrigramsArray.contains(s2)) {
				score2++;
			}
		}
		
		if(score1>score2) {
			return 1;
		}
		else
			return 2;
	}

	
	/**
	 * Reads the given fileName(.txt file) and place the N-Grams into the specified arrayList. 
	 * Argument "max" indicates the maximum number of entries to be loaded into the specified list.
	 * If (max==0), the function load ALL the N-grams in the .txt file.
	 * else, the function load (max) number of N-grams in the .txt file.
	 * 
	 * NOTE:
	 * 1. It is assumed that the N-grams in the .txt files are in separate lines.
	 * 2. It is assumed that the N-grams are sorted beginning with the most frequent N-grams.
	 * 
	 * @param fileName	the filename of the text file containing the frequency of the N-grams
	 * @param arrayList	the arrayList to store the N-gram and sorted in descending order of frequency
	 * @param max		an integer, indicating the maximum number of entries to be stored in the arrayList
	 */
	public static void loadFrequencyFile(String fileName, List<String> arrayList, int max) throws IOException {
		try (Scanner sc = new Scanner(new File(fileName))) {
			int i = 0;
			if(max == 0) {
				//loads the entire frequency file
				while (sc.hasNextLine()) {
		            String line = sc.nextLine();
		            line = line.toLowerCase();
					String[] temp = line.split("\t");
					String s = temp[0];
					arrayList.add(s);
					i++;
		        }
			}
			else {
				//loads the specified max number of entries. (i.e. if(max==30), load first 30 entries from frequency file)
				while (sc.hasNextLine() && i<max) {
					String line = sc.nextLine();
					line = line.toLowerCase();
					String[] temp = line.split("\t");
					String s = temp[0];
					arrayList.add(s);
					i++;
				}
			}
			// note that Scanner suppresses exceptions
	        if (sc.ioException() != null) {
	            throw sc.ioException();
	        }
		}	
	}
	
	
	/**
	 * Swaps the two given keys
	 * 
	 * @param firstKEY	a key to swap mapping with second key
	 * @param secondKEY	a key to swap mapping with first key
	 */
	public static void swapKEY(char firstKEY, char secondKEY) {

		int index1 = mappingKEYArray.indexOf(firstKEY);
		int index2 = mappingKEYArray.indexOf(secondKEY);
		
		//Swapping and updating the LinkedHashMap Table.
		for(Entry<Character, Character> entry : mappingKEY.entrySet()) {
			if(entry.getValue() == firstKEY) {
				entry.setValue(secondKEY);
			}
			else if(entry.getValue() == secondKEY) {
				entry.setValue(firstKEY);
			}
		}
		
		//Swapping and updating the Array List of KEYs.
		mappingKEYArray.set(index1, secondKEY);
		mappingKEYArray.set(index2, firstKEY);
	}

	/**
	 * A private method to check if the specified char is a Latin English Letter
	 * 
	 * @param c 		a char to be checked
	 * @return True 	if 'c' is a Latin English Letter
	 * @return False 	if 'c' is not a Latin English Letter
	 */
	private static boolean isLatinLetter(char c) {
	    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}
}