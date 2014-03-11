

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;


public class Cryptogram {
	
	private char[] cipherText = new char[10000];	//creates a buffer for 10,000 characters
	private	LinkedHashMap<Character, Float> cipherLetterFrequency = new LinkedHashMap<Character, Float>();
	private	LinkedHashMap<String, Float> cipherDigramFrequency = new LinkedHashMap<String, Float>();
	private	LinkedHashMap<String, Float> cipherTrigramFrequency = new LinkedHashMap<String, Float>();
	private	LinkedHashMap<String, Float> cipherTetragramFrequency = new LinkedHashMap<String, Float>();
	
	/*
	 * Default Constructor
	 */
	public Cryptogram() {
	}
	
	/*
	 * Constructor and loads the cipherText into a cipher Buffer
	 * Calls upon the calculateFrequency() and calculateDigramFrequency() to calculate the
	 * relative frequency of letters and digrams in the cipherText 
	 */
	public Cryptogram(String fileName) {
		loadCipherText(fileName);
		cipherLetterFrequency = calculateFrequency(cipherText);
		cipherDigramFrequency = calculateDigramFrequency(cipherText);
		cipherTrigramFrequency = calculateTrigramFrequency(cipherText);
		cipherTetragramFrequency = calculateTetragramFrequency(cipherText);
	}
	
	/*
	 * Public method to load the cipherText into the cipher buffer.
	 * Called upon in the constructor
	 */
	public void loadCipherText(String fileName) {
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
			
			while ((fileReader.read())!=-1) {
				fileReader.read(cipherText);
			}
			fileReader.close();		
			
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
	 * Returns the HashMap Table of the relative frequency of TETRAGRAMS
	 */
	public LinkedHashMap<String, Float> getTetragramFrequency() {
		return cipherTetragramFrequency;
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
			
			if(Character.isLetter(c1)) {
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
	
	public LinkedHashMap<String, Float> calculateTrigramFrequency(char[] buffer) {

		LinkedHashMap<String, Float> hashTable = new LinkedHashMap<String, Float>();

		String trigram = "";
		for(int i=0; i<(buffer.length-2); i++) {
			char c1 = buffer[i];
			c1 = Character.toUpperCase(c1);
			Float val;

			if(Character.isLetter(c1)) {
				trigram = trigram + c1;
				if(trigram.length() == 3) {
					//count the DIGRAMS
					val = hashTable.get(new String(trigram));
					if(val != null) {
						hashTable.put(trigram, val+1);
					}
					else {
						hashTable.put(trigram, (float) 1);
					}
					trigram =  "";	//reset the digram field
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

	public LinkedHashMap<String, Float> calculateTetragramFrequency(char[] buffer) {

		LinkedHashMap<String, Float> hashTable = new LinkedHashMap<String, Float>();

		String tetragram = "";
		for(int i=0; i<(buffer.length-3); i++) {
			char c1 = buffer[i];
			c1 = Character.toUpperCase(c1);
			Float val;

			if(Character.isLetter(c1)) {
				tetragram = tetragram + c1;
				if(tetragram.length() == 4) {
					//count the TETRAGRAMS
					val = hashTable.get(new String(tetragram));
					if(val != null) {
						hashTable.put(tetragram, val+1);
					}
					else {
						hashTable.put(tetragram, (float) 1);
					}
					tetragram =  "";	//reset the digram field
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
	 * Calculates the relative frequency of all LETTERS in the given array of characters
	 */
	public LinkedHashMap<Character, Float> calculateFrequency(char[] buffer) {

		LinkedHashMap<Character, Float> hashTable = new LinkedHashMap<Character, Float>();

		for(int i=0; i<buffer.length; i++) {
			//store each letter in a hash table and count the number of occurrence
			char c = buffer[i];
			Float val;

			c = Character.toUpperCase(c);

			if(Character.isLetter(c)) {	//check if the character is a letter, if it is not, removed them
				val = hashTable.get(new Character(c));

				if(val != null) {
					hashTable.put(c, new Float(val + 1));
				}
				else {
					//character has not been added to hash table or the arrayList
					hashTable.put(c, (float) 1);
				}
			}

		}
		//print the relative frequency of "s", relative to "r"	
		float totalCount = 0;
		for(Entry<Character, Float> entry : hashTable.entrySet()) {
			totalCount += entry.getValue();
		}
		for(Entry<Character, Float> entry : hashTable.entrySet()) {
			entry.setValue((entry.getValue()/totalCount) *100);
		}

		//sorting the hashtable, hashBuffer will contain the sorted hashMap
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

	public void printLetterFrequency() {
		System.out.println(cipherLetterFrequency.toString());
	}

	public void printDigramFrequency() {
		System.out.println(cipherDigramFrequency.toString());
	}

	/*
	 * Returns the corresponding character in the hashtable, using the given index
	 */
	public char getLetter(int index) {
		char c = '*';
		
		for(Entry<Character, Float> entry : cipherLetterFrequency.entrySet()) {
			c=entry.getKey();
			if(index==0) {
				break;
			}
			index--;
		}
		
		return c;
	}
	
	/*
	 * Returns the corresponding String in the hashtable, using the given index
	 */
	public String getDigram(int index) {
		String s = null;
		
		for(Entry<String, Float> entry : cipherDigramFrequency.entrySet()) {
			s=entry.getKey();
			if(index==0) {
				break;
			}
			index--;
		}
		
		return s;
	}	
	
	/*
	 * Returns the corresponding String in the hashtable, using the given index
	 */
	public String getTrigram(int index) {
		String s = null;

		for(Entry<String, Float> entry : cipherTrigramFrequency.entrySet()) {
			s=entry.getKey();
			if(index==0) {
				break;
			}
			index--;
		}

		return s;
	}
	
	/*
	 * Returns the corresponding String in the hashtable, using the given index
	 */
	public String getTetragram(int index) {
		String s = null;

		for(Entry<String, Float> entry : cipherTetragramFrequency.entrySet()) {
			s=entry.getKey();
			if(index==0) {
				break;
			}
			index--;
		}

		return s;
	}	
	
	/*
	 * Decrypt the cipherBuffer with a given key and given size of the message.
	 */
	public char[] decrypt(LinkedHashMap<Character, Character> mappingKEY, int size) {
		
		char[] decryptedMsg;
		
		if(size != 0) {
			decryptedMsg = new char[size];	//decrypt a message with given size defined in the parameter.
			for(int j=0; j<size; j++) {
				decryptedMsg[j] = cipherText[j];
			}	

			for(Entry<Character, Character> entry : mappingKEY.entrySet()) {
				for(int k=0; k<size; k++) {
					if(Character.isLetter(decryptedMsg[k])) {
						char c1 = entry.getValue();
						//c1 = Character.toLowerCase(c1);
						if(decryptedMsg[k] == c1) {
							decryptedMsg[k] = Character.toLowerCase(entry.getKey());
						}
					}
				}
			}
		}
		else {
			decryptedMsg = new char[cipherText.length];
			for(int j=0; j<size; j++) {
				decryptedMsg[j] = cipherText[j];
			}	

			for(Entry<Character, Character> entry : mappingKEY.entrySet()) {
				for(int k=0; k<size; k++) {
					if(Character.isLetter(decryptedMsg[k])) {
						char c1 = entry.getValue();
						//c1 = Character.toLowerCase(c1);
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
