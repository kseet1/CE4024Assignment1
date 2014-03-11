import java.io.*;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Map.Entry;

/*
 * This entity Class contains the the word list of letters, digram data.
 * It contains methods to read a text file and store into a string array.
 * The String array is arranged starting with the most frequent letter/digram to the least frequent
 * (Example: data[0] ->most frequent, data[26] -> least frequent.)
 */
public class WordList {

	private	LinkedHashMap<String, Float> relativeFrequency = new LinkedHashMap<String, Float>();
	
	public WordList() {
		//default constructor
	}
	
	public WordList(String fileName) throws IOException {
		loadFrequencyFile(fileName);
	}

	
	
	/*
	 * Read the given file and store the text content line by line, 
	 */
	public void loadFrequencyFile(String fileName) throws IOException {
		try (Scanner sc = new Scanner(new File(fileName))) {
			while (sc.hasNextLine()) {
	            String line = sc.nextLine();
	            line = line.toLowerCase();
				String[] temp = line.split(",");
	            if(temp.length == 2) {	//for storing common letters/digrams with relative frequency
					relativeFrequency.put(temp[0], Float.parseFloat(temp[1]));		
				}
				else {	//for storing of dictionary of words
					relativeFrequency.put(temp[0], (float)0);
				}
	        }
			// note that Scanner suppresses exceptions
	        if (sc.ioException() != null) {
	            throw sc.ioException();
	        }
		}	
	}
	
	public LinkedHashMap<String, Float> getFrequency() {
		return relativeFrequency;
	}
}
