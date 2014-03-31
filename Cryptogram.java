/**
 * This is a Class Object for holding the data of the cipher text
 * 
 * @author SEET YONG SONG, KENNY
 * @author TAY JIN HENG
 * @author YEOH KENG WEI
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;


public class Cryptogram {
	
	private char[] cipherText;	//creates a buffer for 10,000 characters
	private	LinkedHashMap<Character, Float> cipherLetterFrequency = new LinkedHashMap<Character, Float>();
	private	LinkedHashMap<String, Float> cipherDigramFrequency = new LinkedHashMap<String, Float>();
	
	/**
	 * Default Constructor
	 */
	public Cryptogram() {
	}
	
	/**
	 * Constructor specifying the filename to load content into a char buffer
	 * Calls upon the calculateFrequency() and calculateDigramFrequency() to calculate the
	 * frequency of letters and digrams in the cipherText
	 * 
	 * @param fileName	the filename of the encrypted text
	 * @see loadCipherText(String)
	 * @see calculateFrequency(char[])
	 * @see calculateDigramFrequency(char[])
	 */
	public Cryptogram(String fileName) {
		loadCipherText(fileName);
		cipherLetterFrequency = calculateFrequency(cipherText);
		cipherDigramFrequency = calculateDigramFrequency(cipherText);
	}
	
	/**
	 * Loads the given filename text and store into the char array, cipherText
	 * Called upon in the constructor
	 * 
	 * @param fileName	the filename of the encrypted text
	 */
	private void loadCipherText(String fileName) {
		
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

	/**
	 * A private method to check if the specified char is a Latin English Letter
	 * 
	 * @param c 		a char to be checked
	 * @return True 	if 'c' is a Latin English Letter
	 * @return False 	if 'c' is not a Latin English Letter
	 */
	private boolean isLatinLetter(char c) {
	    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}
	
	/**
	 * Calculates the frequency of all English letters in the given array of char
	 * Sorts them in a LinkedHashMap in descending order of frequency
	 * 
	 * @param buffer 		an array of char containing the ciphertext
	 * @return hashBuffer	a sorted LinkedHashMap containing the frequency of each letters, sorted in descending order
	 */
	private LinkedHashMap<Character, Float> calculateFrequency(char[] buffer) {

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
		
		//Sorting the LinkedHashMap, variable hashBuffer will contain the sorted LinkedHashMap
		LinkedHashMap<Character, Float> hashBuffer = new LinkedHashMap<Character, Float>();

		//Sorting the LinkedHashMap in descending order of frequency
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

		//Return hashBuffer, a sorted LinkedHashMap
		return hashBuffer;
	}
	
	/**
	 * Calculates the frequency of all the DIGRAMS in the given array of char
	 * Sorts them in a LinkedHashMap in descending order of frequency
	 * 
	 * @param buffer 		an array of char containing the ciphertext
	 * @return hashBuffer	a sorted LinkedHashMap containing the frequency of each digrams, sorted in descending order
	 */
	private LinkedHashMap<String, Float> calculateDigramFrequency(char[] buffer) {
		
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

	/**
	 * A public get method to get the original ciphertext
	 */
	public char[] getText() {
		return cipherText;
	}
	
	/**
	 * A public get method to get the hashMap containing frequency of letters 	 
	 * 
	 * @return cipherLetterFrequency 	a hashMap containing the frequency of letters
	 */
	public LinkedHashMap<Character, Float> getFrequency() {			
		return cipherLetterFrequency;
	}

	/**
	 * A public get method to get the hashMap containing the frequency of digrams
	 * 
	 * @return cipherDigramFrequency	a hashMap containing the frequency of digrams
	 */
	public LinkedHashMap<String, Float> getDigramFrequency() {
		return cipherDigramFrequency;
	}
	
	/**
	 * Decrypt the cipherBuffer with a given key and given size of the message.
	 * If (size==0), the function decrypts and returns the entire ciphertext.
	 * Else, the function decrypts and returns the ciphertext of given size. 
	 * (i.e size==1000, returns a decrypted text of length 1000)
	 * 
	 * @param mappingKEY		a hashMap containing the mapping KEY, to be used for decryption
	 * @param size 				an integer indicating the size of the ciphertext to be decrypted
	 * @return decryptedMsg 	an array of char containing the decrypted ciphertext
	 */
	public char[] decrypt(HashMap<Character, Character> mappingKEY, int size) {
		
		char[] decryptedMsg;
	
		//If size!= 0, decrypt a message given the size defined in the parameter
		if(size != 0) {
			//Store the ciphertext with a given size in a temporary array
			decryptedMsg = new char[size];	
			for(int j=0; j<size; j++) {
				decryptedMsg[j] = cipherText[j];
			}	

			//Decrypt and modify the temporary array
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
		
		//else, size == 0, decrypt the entire ciphertext
		else {
			//Store the entire ciphertext in a temporary array
			decryptedMsg = new char[cipherText.length];
			for(int j=0; j<cipherText.length; j++) {
				decryptedMsg[j] = cipherText[j];
			}	

			//Decrypt and modify the temporary array
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
		//Return the temporary array containing the decrypted text
		return decryptedMsg;
	}

}
