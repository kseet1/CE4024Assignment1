import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

public class MainController {
	
	private static char[] alphabets = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	private static char[] key = new char[26];
	
	private static LinkedHashMap<Character, Character> mappingKEY = new LinkedHashMap<Character, Character>();	//HashMap<Plaintext, Ciphertext>
	private static ArrayList<Character> mappingKEYArray = new ArrayList<Character>();
	private static final int bufferSize = 1500;
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
//	private static List<String> commonDigramsArray = Arrays.asList(commonDigrams);
//	private static List<String> commonTrigramsArray = Arrays.asList(commonTrigrams);
	private static List<String> commonQuadrigramsArray = Arrays.asList(commonQuadrigrams);
	
//	private static List<String> commonLettersArray = new ArrayList<String>();
//	private static List<String> commonQuadrigramsArray = new ArrayList<String>();
	private static List<String> commonDigramsArray = new ArrayList<String>();
	private static List<String> commonTrigramsArray = new ArrayList<String>();
	private static List<String> DigramsArray = new ArrayList<String>();
	private static List<String> TrigramsArray = new ArrayList<String>();
	
	public static void main(String[] args) throws IOException {
		
		//WordList dictionary = new WordList("dictionary.txt");
	    loadFrequencyFile("digramFrequency.txt", commonDigramsArray, 30);
	    loadFrequencyFile("trigramFrequency.txt", commonTrigramsArray, 30);
		loadFrequencyFile("digramFrequency.txt",DigramsArray, 0);
		loadFrequencyFile("trigramFrequency.txt",TrigramsArray,0);
		
		//Menu
		System.out.println("Please enter the encrypted text file name (Including the file type (.txt): ");
		Scanner sc = new Scanner(System.in);
		String fileName = sc.next();
		
		Cryptogram ciphertext = new Cryptogram(fileName);				
	
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
		
		int repeatCount = 0;
		do{
			for(int increment=1; increment<26; increment++) {
				for(int k=0; k<25; k++) {
					if(k+increment > 25) {
						break;
					}
					char key1 = mappingKEYArray.get(k);
					char key2 = mappingKEYArray.get(k+increment);
					swapKEY(key1, key2);
					decryptedBuffer2 = ciphertext.decrypt(mappingKEY, bufferSize);

					//compare the two decryptedBuffers
					if(repeatCount<8) {
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
			System.out.println("\n Decrypting... Please wait...");
			repeatCount++;
		}while(repeatCount<12);
	
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
			System.out.println("2. Manually swap Keys");
			System.out.println("3. View Key");
			System.out.println("4. Exit the program");
			System.out.println("===============================================");

			System.out.print("Option: ");
			int choice = sc.nextInt();
			switch(choice){
			case 1:
				System.out.println(ciphertext.decrypt(mappingKEY,0));
				break;
			case 2: {
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
			case 3: {
				System.out.println("\nKEY Mapping: ");
				System.out.println(mappingKEY.toString());
				System.out.println("Plaintext: " + new String(alphabets));
				System.out.println("Ciphertext: " + new String(key));
				break;
			}
			case 4: {
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

}