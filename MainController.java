import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

public class MainController {
	
	private static char[] alphabets = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	private static char[] key = new char[26];
	
	private static LinkedHashMap<Character, Character> mappingKEY = new LinkedHashMap<Character, Character>();	//HashMap<Plaintext, Ciphertext>
	private static ArrayList<Character> mappingKEYArray = new ArrayList<Character>();
	private static final int bufferSize = 2000;
	private static char[] decryptedBuffer1 = new char[bufferSize];
	private static char[] decryptedBuffer2 = new char[bufferSize];
	
	private static String[] commonLetters = {"e","t","a","o","i","n","s","r","h","d","l","u","c","m","f","y","w","g","p","b","v","k","x","q","j","z"};
	
	private static String[] commonDigrams = {"th","he","in","er","an","re","nd","at","on","nt","ha","es","st","en","ed","to","it","ou","ea","hi","is",
											"or","ti","as","te","et","ng","of","al","de","se","le","sa","si","ar","ve","ra","ld","ur"};

	private static String[] commonTrigrams = {"the","ing","and","hat","tha","ion","you","ent","for","tio","thi","her","ati","our","ere","all","ter",
											"ver","not","hin","ome","oul","uld","int","rea","pro","res","ate","hav","ave","ill","his","com","ons",
											"are","ple","ers","con","ess","out","one","ith","som","ive","tin","nce","ble","ted","han"};
	
	private static String[] commonQuadrigrams = {"that","ther","with","tion","here","ould","ight","have","hich","whic","this","thin","they","atio",
											"ever","from","ough","were","hing","ment", "quite"};	
	
	private static List<String> commonLettersArray = Arrays.asList(commonLetters);
	private static List<String> commonDigramsArray = Arrays.asList(commonDigrams);
	private static List<String> commonTrigramsArray = Arrays.asList(commonTrigrams);
	private static List<String> commonQuadrigramsArray = Arrays.asList(commonQuadrigrams);
	
	private static List<String> DigramsArray = new ArrayList<String>();
	private static List<String> TrigramsArray = new ArrayList<String>();
	
	public static void main(String[] args) throws IOException {
		
		Set<String> dictionary = new HashSet<String>();
	    Scanner filescan = new Scanner(new File("dictionary.txt"));
	    while (filescan.hasNext()) {
	        dictionary.add(filescan.nextLine().toLowerCase());
	    }
	    filescan.close();
		
		//WordList dictionary = new WordList("dictionary.txt");
		loadFrequencyFile("digramFrequency.txt",DigramsArray, 0);
		loadFrequencyFile("trigramFrequency.txt",TrigramsArray,300);
		
		//Menu
		System.out.println("Please enter the encrypted text file name (Including the file type (.txt): ");
		Scanner sc = new Scanner(System.in);
		String fileName = sc.next();
		System.out.println(fileName);
		
		Cryptogram ciphertext = new Cryptogram(fileName);				
		
		// Step 1: Map the most common letter in english ('e') to the most common cipher found
		mappingKEY.put(commonLettersArray.get(0).charAt(0), ciphertext.getLetter(0));
		mappingKEYArray.add(ciphertext.getLetter(0));
		mapDigrams(ciphertext);
		mapDigrams(ciphertext);
		
		int startingIndex = mappingKEY.size()-1;

		int index = 0;
		for(Entry<Character, Float> entry : ciphertext.getFrequency().entrySet()) {
			if(mappingKEYArray.contains(entry.getKey())) {
				//skips, do not map the current key
			}
			else {
				while(mappingKEY.containsKey(commonLettersArray.get(index).charAt(0))) {
					index++;	//keep trying next index, till it can add.
				}
				mappingKEY.put(commonLettersArray.get(index).charAt(0), entry.getKey());
				mappingKEYArray.add(entry.getKey());
				index++;

			}
		}
		
		System.out.println("\n Initial KEY Mapping: ");
		System.out.println(mappingKEY.toString());
		decryptedBuffer1 = ciphertext.decrypt(mappingKEY, bufferSize); 
		
		int repeats = 0;
		do{
			for(int increment=1; increment<26; increment++) {
				for(int k=startingIndex; k<25; k++) {
					if(k+increment > 25) {
						break;
					}
					char key1 = mappingKEYArray.get(k);
					char key2 = mappingKEYArray.get(k+increment);
					swapKEY(key1, key2);
					decryptedBuffer2 = ciphertext.decrypt(mappingKEY, bufferSize);

					//compare the two decryptedBuffers
					if(repeats<8) {
						if(compare(decryptedBuffer1, decryptedBuffer2) == 1) {
							swapKEY(key1, key2);	//swap back the key
						}
						else {
							decryptedBuffer1 = decryptedBuffer2;
						}
					}
					else {
						if(compareFull(decryptedBuffer1, decryptedBuffer2) == 1) {
							swapKEY(key1, key2);	//swap back the key
						}
						else {
							decryptedBuffer1 = decryptedBuffer2;
						}
					}
				}
			}
			
			System.out.println(decryptedBuffer1);
			startingIndex = 0;
			repeats++;
		}while(repeats<12);
	
		for(int i=0; i<alphabets.length; i++) {
			key[i] = mappingKEY.get(alphabets[i]);
		}
		System.out.println("\nKEY Mapping: ");
		System.out.println(mappingKEY.toString());
		System.out.println("Plaintext: " + new String(alphabets));
		System.out.println("Key: " + new String(key));
		
		while(true) {
			System.out.println("\n===============================================");
			System.out.println("Please select an option (1-5): ");
			System.out.println("1. Print the decrypted text");
			System.out.println("2. Print the letter frequency");
			System.out.println("3. Manually swap Keys");
			System.out.println("4. View Key");
			System.out.println("5. Exit the program");
			System.out.println("===============================================");

			System.out.print("Option: ");
			int choice = sc.nextInt();
			switch(choice){
			case 1:
				System.out.println(ciphertext.decrypt(mappingKEY,10000));
				break;
			case 2:
				break;
			case 3: {
				System.out.println("\nPlaintext: " + new String(alphabets));
				System.out.println("Key: " + new String(key));
				System.out.println("\nSelect the two KEYs you wish to swap: ");
				System.out.print("First key: ");
				String input;
				input = sc.next();
				char key1 = input.charAt(0);
				System.out.print("Second key: ");
				input = sc.next();
				char key2 = input.charAt(0);
				
				swapKEY(mappingKEY.get(key1), mappingKEY.get(key2));
				//update the key array
				for(int i=0; i<alphabets.length; i++) {
					key[i] = mappingKEY.get(alphabets[i]);
				}
				System.out.println("Swapping done!");
				break;
			}
			case 4: {
				System.out.println("\nKEY Mapping: ");
				System.out.println(mappingKEY.toString());
				System.out.println("Plaintext: " + new String(alphabets));
				System.out.println("Ciphertext: " + new String(key));
				break;
			}
			case 5: {
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

	public static void search(String input, Set<String> dictionary,
	        Stack<String> words, List<List<String>> results) {

	    for (int i = 0; i < input.length(); i++) {
	        // take the first i characters of the input and see if it is a word
	        String substring = input.substring(0, i + 1);

	        if (dictionary.contains(substring)) {
	            // the beginning of the input matches a word, store on stack
	            words.push(substring);

	            if (i == input.length() - 1) {
	                // there's no input left, copy the words stack to results
	                results.add(new ArrayList<String>(words));
	            } else {
	                // there's more input left, search the remaining part
	                search(input.substring(i + 1), dictionary, words, results);
	            }

	            // pop the matched word back off so we can move onto the next i
	            words.pop();
	        }
	    }
	}
	
	/*
	 * Compare the two given decrypted buffer and return the highest score buffer
	 */
	public static int compare(char[] decryptedBuffer1, char[] decryptedBuffer2) {
		int score1 = 0;
		int score2 = 0;
		
		//calculate the score based on digram
		for(int i=0; i<bufferSize-1; i++) {
			String s1 = "" + decryptedBuffer1[i] + decryptedBuffer1[i+1];
			String s2 = "" + decryptedBuffer2[i] + decryptedBuffer2[i+1];
		
			if(commonDigramsArray.contains(s1) && commonDigramsArray.contains(s2)) {
				if(commonDigramsArray.indexOf(s1) < commonDigramsArray.indexOf(s2)) {
					score1++;
				}
				else if(commonDigramsArray.indexOf(s1) > commonDigramsArray.indexOf(s2)) {
					score2++;
				}
			} else if(commonDigramsArray.contains(s1)) {
				score1++;
			} else if (commonDigramsArray.contains(s2)) {
				score2++;
			} 
		}
		
		//calculate the score based on Trigram
		for(int i=0; i<bufferSize-2; i++) {
			String s1 ="" + decryptedBuffer1[i] + decryptedBuffer1[i+1] + decryptedBuffer1[i+2];
			String s2 ="" + decryptedBuffer2[i] + decryptedBuffer2[i+1] + decryptedBuffer2[i+2];
			
			if(commonTrigramsArray.contains(s1) && commonTrigramsArray.contains(s2)) {
				if(commonTrigramsArray.indexOf(s1) < commonTrigramsArray.indexOf(s2)) {
					score1++;
				}
				else if(commonTrigramsArray.indexOf(s1) > commonTrigramsArray.indexOf(s2)){
					score2++;
				}
			} else if(commonTrigramsArray.contains(s1)) {
				score1++;
			} else if(commonTrigramsArray.contains(s2)) {
				score2++;
			}
		}
		
		//calculate the score based on Quadrigram
		for(int i=0; i<bufferSize-3; i++) {
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

	/*
	 * Compare the two given decrypted buffer and return the highest score buffer
	 */
	public static int compareFull(char[] decryptedBuffer1, char[] decryptedBuffer2) {
		int score1 = 0;
		int score2 = 0;
		
		//calculate the score based on digram
		for(int i=0; i<bufferSize-1; i++) {
			String s1 ="" + decryptedBuffer1[i] + decryptedBuffer1[i+1];
			String s2 ="" + decryptedBuffer2[i] + decryptedBuffer2[i+1];
			
			if(DigramsArray.contains(s1) && DigramsArray.contains(s2)) {
				if(DigramsArray.indexOf(s1) < DigramsArray.indexOf(s2)) {
					score1++;
				}
				else if(DigramsArray.indexOf(s1) > DigramsArray.indexOf(s2)) {
					score2++;
				}
			} else if(DigramsArray.contains(s1)) {
				score1++;
			} else if (DigramsArray.contains(s2)) {
				score2++;
			} 
		}
		
		//calculate the score based on Trigram
		for(int i=0; i<bufferSize-2; i++) {
			String s1 ="" + decryptedBuffer1[i] + decryptedBuffer1[i+1] + decryptedBuffer1[i+2];
			String s2 ="" + decryptedBuffer2[i] + decryptedBuffer2[i+1] + decryptedBuffer2[i+2];
			
			if(TrigramsArray.contains(s1) && TrigramsArray.contains(s2)) {
				if(TrigramsArray.indexOf(s1) < TrigramsArray.indexOf(s2)) {
					score1++;
				}
				else if(TrigramsArray.indexOf(s1) > TrigramsArray.indexOf(s2)){
					score2++;
				}
			} else if(TrigramsArray.contains(s1)) {
				score1++;
			} else if(TrigramsArray.contains(s2)) {
				score2++;
			}
		}
		
		//calculate the score based on Quadrigram
		for(int i=0; i<bufferSize-3; i++) {
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
	
	/*
	 * getword
	 */
	public static String getWord(int index, int length, char[] decryptedBuffer) {
		String s = "";
		if(index + length >= bufferSize) {
			return null;
		}
		while(s.length() != length) {
			s = s + decryptedBuffer[index];
			index++;
		}
		return s;
	}
	
	/*
	 * Reads the given fileName(.txt file) and place the N-Grams into the specified arrayList. 
	 * Argument "max" indicates the maximum number of entries to be loaded into the specified list.
	 * If (max==0), the function load ALL the N-grams in the .txt file.
	 * else, the function load (max) number of N-grams in the .txt file.
	 * 
	 * NOTE:
	 * 1. It is assumed that the N-grams in the .txt files are in separate lines.
	 * 2. It is assumed that the N-grams are sorted beginning with the most frequent N-grams.
	 */
	public static void loadFrequencyFile(String fileName, List<String> arrayList, int max) throws IOException {
		try (Scanner sc = new Scanner(new File(fileName))) {
			int i = 0;
			if(max == 0) {
				while (sc.hasNextLine()) {
		            String line = sc.nextLine();
		            line = line.toLowerCase();
					String[] temp = line.split("\t");
					String s = temp[0];
					if(s.length() == 2) {
						arrayList.add(s);
					} else if(s.length() == 3) {
						arrayList.add(s);
					}
					i++;
		        }
			}
			else {	
				while (sc.hasNextLine() && i<max) {
					String line = sc.nextLine();
					line = line.toLowerCase();
					String[] temp = line.split("\t");
					String s = temp[0];
					if(s.length() == 2) {
						arrayList.add(s);
					} else if(s.length() == 3) {
						arrayList.add(s);
					}
					i++;
				}
			}
			// note that Scanner suppresses exceptions
	        if (sc.ioException() != null) {
	            throw sc.ioException();
	        }
		}	
	}
	
	/*
	 * ===========================================
	 * Swaps the two keys, given by the keys
	 * ===========================================
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
	
	/*
	 * Maps the most common unmapped English DIGRAM to the most common unmapped cipher DIGRAM
	 */
	public static void mapDigrams(Cryptogram cryptogram) {
		//look at the next possible digram
		String s1 = null;
		String s2 = null;
		boolean found1 = false;
		boolean found2 = false;
		
		for(int i=0; i<commonDigramsArray.size(); i++) {
			s1 = commonDigramsArray.get(i);
			if((mappingKEY.containsKey(s1.charAt(0))) || (mappingKEY.containsKey(s1.charAt(1)))) {
				//repick
			}
			else {
				found1 = true;
				break;
			}
		}
		
		for(int i=0; i<cryptogram.getDigramFrequency().size(); i++) {
			s2 = cryptogram.getDigram(i);
			if((mappingKEY.containsValue(s2.charAt(0))) || (mappingKEY.containsValue(s2.charAt(1)))) {
				//repick
			}
			else {
				found2 = true;
				break;
			}
		}
		
		//mapping the key for the digram
		if(found1&&found2) {
			for(int i=0; i<s1.length(); i++) {
				mappingKEY.put(s1.charAt(i), s2.charAt(i));	
				mappingKEYArray.add(s2.charAt(i));
			}
		}
	}

	/*
	 * Maps the most common unmapped English TRIGRAM to the most common unmapped cipher TRIGRAM
	 */
	public static void mapTrigrams(Cryptogram cryptogram) {
		//look at the next possible digram
		String s1 = null;
		String s2 = null;
		boolean found1 = false;
		boolean found2 = false;
		
		for(int i=0; i<commonTrigramsArray.size(); i++) {
			s1 = commonTrigramsArray.get(i);
			if((mappingKEY.containsKey(s1.charAt(0))) || (mappingKEY.containsKey(s1.charAt(1)) || (mappingKEY.containsKey(s1.charAt(2))))) {
				//repick
			}
			else {
				found1 = true;
				break;
			}
		}
		
		for(int i=0; i<cryptogram.getTrigramFrequency().size(); i++) {
			s2 = cryptogram.getTrigram(i);
			if((mappingKEY.containsValue(s2.charAt(0))) || (mappingKEY.containsValue(s2.charAt(1)) || (mappingKEY.containsValue(s2.charAt(2))))) {
				//repick
			}
			else {
				found2 = true;
				break;
			}
		}
		
		//mapping the key for the digram
		if(found1&&found2) {
			for(int i=0; i<s1.length(); i++) {
				mappingKEY.put(s1.charAt(i), s2.charAt(i));
				mappingKEYArray.add(s2.charAt(i));
			}
		}
	}

	/*
	 * Maps the most common unmapped English TETRAGRAM to the most common unmapped cipher TETRAGRAM
	 */
	public static void mapQuadrigrams(Cryptogram cryptogram) {
		//look at the next possible quadrigram
		String s1 = null;
		String s2 = null;
		boolean found1 = false;
		boolean found2 = false;
		
		for(int i=0; i<commonQuadrigramsArray.size(); i++) {
			s1 = commonQuadrigramsArray.get(i);
			if((mappingKEY.containsKey(s1.charAt(0))) || (mappingKEY.containsKey(s1.charAt(1)) || (mappingKEY.containsKey(s1.charAt(2)) || mappingKEY.containsKey(s1.charAt(3))))) {
				//repick
			}
			else {
				found1 = true;
				break;
			}
		}
		
		for(int i=0; i<cryptogram.getTetragramFrequency().size(); i++) {
			s2 = cryptogram.getTetragram(i);
			if((mappingKEY.containsValue(s2.charAt(0))) || (mappingKEY.containsValue(s2.charAt(1)) || (mappingKEY.containsValue(s2.charAt(2)) || mappingKEY.containsKey(s2.charAt(3))))) {
				//repick
			}
			else {
				found2 = true;
				break;
			}
		}
		
		//mapping the key for the digram
		if(found1&&found2) {
			for(int i=0; i<s1.length(); i++) {
				mappingKEY.put(s1.charAt(i), s2.charAt(i));
				mappingKEYArray.add(s2.charAt(i));
			}
		}
	}
	
}
