

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;


public class Cryptogram {
	
	private char[] cipherText;	//creates a buffer for 10,000 characters
	private	LinkedHashMap<Character, Float> cipherLetterFrequency = new LinkedHashMap<Character, Float>();
	private	LinkedHashMap<String, Float> cipherDigramFrequency = new LinkedHashMap<String, Float>();
	private	LinkedHashMap<String, Float> cipherTrigramFrequency = new LinkedHashMap<String, Float>();
	
	/*
	 * Default Constructor
	 */
	public Cryptogram() {
	}
	
	/*
	 * =============================================================================================
	 * Constructor and loads the cipherText into a cipher Buffer
	 * Calls upon the calculateFrequency() and calculateDigramFrequency() to calculate the
	 * relative frequency of letters and digrams in the cipherText 
	 * =============================================================================================
	 */
	public Cryptogram(String fileName) {
		loadCipherText(fileName);
		cipherLetterFrequency = calculateFrequency(cipherText);
		cipherDigramFrequency = calculateDigramFrequency(cipherText);
	}
	
	/*
	 * =============================================================================================
	 * Loads the given filename text into the cipher buffer.
	 * Called upon in the constructor
	 * =============================================================================================
	 */
	public void loadCipherText(String fileName) {
		
		ArrayList<Character> buffer = new ArrayList<Character>();
		
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
			
			do {
				char c = (char) fileReader.read();
				
				if(c != (char) -1) {
					buffer.add(c);
				}
				else {
					break;
				}
			}while(true);
			
			fileReader.close();		
			
			//store from buffer into char Array variable cipherText[]
			cipherText = new char[buffer.size()];
			for(int i=0; i<buffer.size(); i++) {
				cipherText[i] = buffer.get(i);
			}
			
			//change all to UPPERCASE
			for(int i=0; i<cipherText.length; i++) {
				cipherText[i] = Character.toUpperCase(cipherText[i]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/*
	 * Returns the HashMap Table of the relative frequency of LETTERS 	 
	 */
	public LinkedHashMap<Character, Float> getFrequency() {			
		return cipherLetterFrequency;
	}

	/*
	 * Returns the HashMap Table of the relative frequency of DIGRAMS 	 
	 */
	public LinkedHashMap<String, Float> getDigramFrequency() {
		return cipherDigramFrequency;
	}
	
	/*
	 * Returns the HashMap Table of the relative frequency of TRIGRAMS
	 */
	public LinkedHashMap<String, Float> getTrigramFrequency() {
		return cipherTrigramFrequency;
	}
	
	/*
	 * returns True if the character is a Latin English letter
	 */
	public static boolean isLatinLetter(char c) {
	    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}
	
	/*
	 * Calculates the relative frequency of all the DIGRAMS in the given array of characters
	 */
	public LinkedHashMap<String, Float> calculateDigramFrequency(char[] buffer) {
		
		LinkedHashMap<String, Float> hashTable = new LinkedHashMap<String, Float>();
		
		String digram = "";
		for(int i=0; i<(buffer.length-1); i++) {
			char c1 = buffer[i];
			c1 = Character.toUpperCase(c1);
			Float val;
			
			if(isLatinLetter(c1)) {
				digram = digram + c1;
				if(digram.length() == 2) {
					//count the DIGRAMS
					val = hashTable.get(new String(digram));
					if(val != null) {
						hashTable.put(digram, val+1);
					}
					else {
						hashTable.put(digram, (float) 1);
					}
					digram =  "";	//reset the digram field
				}
			}
			else {
				//character is not a letter -> do nothing - restart the loop and look at next character
			}
		}
		
		//calculate the relative frequency of the digrams
		float totalCount = 0;
		for(Entry<String, Float> entry : hashTable.entrySet()) {
			totalCount += entry.getValue();
		}
		for(Entry<String, Float> entry: hashTable.entrySet()) {
			entry.setValue((entry.getValue()/totalCount) *100);
		}
		
		//sorting the hashtable, hashBuffer will contain the new sorted hashMap
		LinkedHashMap<String, Float> hashBuffer = new LinkedHashMap<String, Float>();
		
		while(hashTable.size() != 0) {	
			Entry<String, Float> maxEntry = null;
			for(Entry<String, Float> entry : hashTable.entrySet()) {
				if(maxEntry == null || maxEntry.getValue() < entry.getValue()) {
					maxEntry = entry;
				}
			}
			hashBuffer.put(maxEntry.getKey(), maxEntry.getValue());
			hashTable.remove(maxEntry.getKey());
		}
		return hashBuffer;
	}

	/*
	 * Calculates the frequency of all English letters in the given array of characters
	 * Sorts them in a hash table in descending order
	 */
	public LinkedHashMap<Character, Float> calculateFrequency(char[] buffer) {

		LinkedHashMap<Character, Float> hashTable = new LinkedHashMap<Character, Float>();

		for(int i=0; i<buffer.length; i++) {
			//store each English letter in a hash table and count the number of occurrence
			char c = buffer[i];
			Float val;

			c = Character.toUpperCase(c);

			//check if the character is a letter, if it is not, ignore them
			if(isLatinLetter(c)) {	
				val = hashTable.get(new Character(c));

				if(val != null) {
					//character has already been added into hash table, increment counter
					hashTable.put(c, new Float(val + 1));
				}
				else {
					//character has not been added to hash table or the arrayList
					hashTable.put(c, (float) 1);
				}
			}

		}
		
		/*//calculating the relative frequency	
		float totalCount = 0;
		for(Entry<Character, Float> entry : hashTable.entrySet()) {
			totalCount += entry.getValue();
		}
		for(Entry<Character, Float> entry : hashTable.entrySet()) {
			entry.setValue((entry.getValue()/totalCount) *100);
		}
*/
		//sorting the hashtable, variable hashBuffer will contain the sorted hashMap
		LinkedHashMap<Character, Float> hashBuffer = new LinkedHashMap<Character, Float>();

		while(hashTable.size() != 0) {	
			Entry<Character, Float> maxEntry = null;
			for(Entry<Character, Float> entry : hashTable.entrySet()) {
				if(maxEntry == null || maxEntry.getValue() < entry.getValue()) {
					maxEntry = entry;
				}
			}
			hashBuffer.put(maxEntry.getKey(), maxEntry.getValue());
			hashTable.remove(maxEntry.getKey());
		}

		return hashBuffer;
	}
	
	/*
	 * Decrypt the cipherBuffer with a given key and given size of the message.
	 * If (size==0), the function decrypts and returns the entire ciphertext.
	 * else, the function decrypts and returns the ciphertext of given size. 
	 * (i.e size==1000, returns a decrypted text of length 1000)
	 */
	public char[] decrypt(LinkedHashMap<Character, Character> mappingKEY, int size) {
		
		char[] decryptedMsg;
		
		if(size != 0) {
			//decrypt a message given the size defined in the parameter
			
			decryptedMsg = new char[size];	
			for(int j=0; j<size; j++) {
				decryptedMsg[j] = cipherText[j];
			}	

			for(Entry<Character, Character> entry : mappingKEY.entrySet()) {
				for(int k=0; k<size; k++) {
					if(Character.isLetter(decryptedMsg[k])) {
						char c1 = entry.getValue();
						if(decryptedMsg[k] == c1) {
							decryptedMsg[k] = Character.toLowerCase(entry.getKey());
						}
					}
				}
			}
		}
		else {
			// size == 0, decrypts the entire ciphertext
	
			decryptedMsg = new char[cipherText.length];
			for(int j=0; j<cipherText.length; j++) {
				decryptedMsg[j] = cipherText[j];
			}	

			for(Entry<Character, Character> entry : mappingKEY.entrySet()) {
				for(int k=0; k<cipherText.length; k++) {
					if(Character.isLetter(decryptedMsg[k])) {
						char c1 = entry.getValue();
						if(decryptedMsg[k] == c1) {
							decryptedMsg[k] = Character.toLowerCase(entry.getKey());
						}
					}
				}
			}
		}
		return decryptedMsg;
	}

}
