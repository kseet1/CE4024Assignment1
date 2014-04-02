import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

// This is a class template for Assignment 2 for the course
// CCE4024/CPE413/CSC409/CZ4024 - Cryptography and Network Security



public class Assignment2 {
	public static int PASSWORD_MAX_LENGTH = 8;
	public static int PASSWORD_MIN_LENGTH = 5;
	public static HashMap<Integer, ArrayList<String>> dictionary = new HashMap<Integer, ArrayList<String>>();
	public static ArrayList<Hash> hashes = new ArrayList<Hash>();
	public static BlockingQueue<String> wordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> passwordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> validatedQ = new LinkedBlockingQueue<String>();
	public static ExecutorService passwordTransformThreadPool = Executors.newFixedThreadPool(1);
	public static ExecutorService passwordValidationThreadPool = Executors.newFixedThreadPool(4);
	
	private static class Hash {
		public String username;
		public String id;
		public String salt;
		public String hashValue;
		public String cryptSalt;
		public String hashCheck;
		
		public Hash(String username, String id, String salt, String hashValue) {
			this.username = username;
			this.id = id;
			this.salt = salt;
			this.hashValue = hashValue;
			this.cryptSalt = "$"+id+"$"+salt;
			this.hashCheck = this.cryptSalt+"$"+hashValue;
		}
		
		@Override
		public boolean equals(Object object) {
			if (object != null && object instanceof String) {
				return object.equals(this.hashCheck);
			}
			if (object != null && object instanceof Hash) {
				return ((Hash)object).hashCheck.equals(this.hashCheck);
			}
			return false;
		}
	}
	public static class PasswordTransformThread extends Thread {
		public void run() {
			//System.out.println("Running Password Transform Rule.");
			ArrayList<String> words = new ArrayList<String>();
			for (int i=PASSWORD_MIN_LENGTH; i<=PASSWORD_MAX_LENGTH; i++) {
				words.addAll(dictionary.get(i));
			}
			for (String word : words) {
				//character_substitution_rule(word);
			}
			multiple_words_rule();
			words.clear();
			for (int i=PASSWORD_MIN_LENGTH-2; i<=PASSWORD_MAX_LENGTH-2; i++) {
				words.addAll(dictionary.get(i));
			}
			for (String word : words) {
				//number_prefix_suffix_rule(word);
			}
		}
		public void character_substitution_rule(String word) {
			/**
			 * i - 1
			 * e - 3
			 * a - 4
			 * s - 5
			 * t - 7
			 * o - 0
			 **/
			LinkedHashSet<String> words = new LinkedHashSet<String>();
			if (word.contains("i"))
				words.add(word.replace("i", "1"));
			if (word.contains("e"))
				words.add(word.replace("e", "3"));
			if (word.contains("a"))
				words.add(word.replace("a", "4"));
			if (word.contains("s"))
				words.add(word.replace("s", "5"));
			if (word.contains("t"))
				words.add(word.replace("t", "7"));
			if (word.contains("o"))
				words.add(word.replace("o", "0"));
			if (word.matches("(.*)[ieasto](.*)"))
				words.add(word.replace("i", "1")
							  .replace("e", "3")
							  .replace("a", "4")
							  .replace("s", "5")
							  .replace("t", "7")
							  .replace("o", "0"));
			for (String password : words) putIntoQueue(passwordQ, password);
		}
		public void multiple_words_rule() {
			String temp = null;
			ArrayList<String> words = new ArrayList<String>();
			for (ArrayList<String> list : dictionary.values())
				words.addAll(list);
			int wSize = words.size();
			
			for (int i=0; i<wSize ; i++) {
				for (int j=0; j<wSize; j++) {
					String s1 = words.get(i);
					String s2 = words.get(j);
					temp = s1 + s2;
					if (temp.length() >= PASSWORD_MIN_LENGTH && 
						temp.length() <= PASSWORD_MAX_LENGTH) {
						putIntoQueue(passwordQ, temp);
						temp = s2 + s1;
						putIntoQueue(passwordQ, temp);
					}
					if (temp.length() <= PASSWORD_MAX_LENGTH) { //still able to append another word
						//search for additional string to append
						addWord(words, temp);
						temp = s2 + s1;
						addWord(words, temp);
					}
					
				}
			}			
		}
		private static void addWord(List<String> words, String word) {
			String temp = null;
			int wSize = words.size();
			for (int j=0; j<wSize;j++) {
				temp = word + words.get(j);
				if (temp.length() >= PASSWORD_MIN_LENGTH &&
						temp.length() <= PASSWORD_MAX_LENGTH) {
					putIntoQueue(passwordQ, temp);
					temp = words.get(j) + word;
					putIntoQueue(passwordQ, temp);
				}
				if (temp.length() <= PASSWORD_MAX_LENGTH) {
					addWord(words, temp);
					temp = words.get(j) + word;
					addWord(words, temp);
				}
			}
			word = temp;
		}
		public void number_prefix_suffix_rule(String word) {
			String temp;
			if (word.matches("(.*)[0-9](.*)")) return; // Omit the password if numbers are already present.
			for (int j=0; j<100; j++) { // Appends or prepends at most 2 numbers.
				temp = word+j;
				putIntoQueue(passwordQ, temp);
				temp = j+word;
				putIntoQueue(passwordQ, temp);
			}
		}
	}
	public static class PasswordValidationThread extends Thread {
		public void run() {
			//long startTime = System.currentTimeMillis();
			//System.out.println("Running Password Validation.");
			String password;
			boolean finished = false;
			while (!finished) {
				if ((password = passwordQ.poll()) != null) {
					for (Hash hashObject : hashes) {
						String hash = Crypt.crypt(password, hashObject.cryptSalt);
						if (hash.equals(hashObject.hashCheck)) {
							System.out.println(hashObject.username+":"+password);
							putIntoQueue(validatedQ, hashObject.username+":"+password);
							hashes.remove(hashObject);
							//break; // Commented because maybe some other users use the same passwords.
						}
					}
				}
				if (passwordQ.size() <= 0) finished = true;
			}
			//long endTime = System.currentTimeMillis();
			//System.out.println((endTime - startTime));
		}
	}
	
	public static void main(String args[])
	{
		
		// SAMPLE CODES: REMOVE FROM YOUR FINAL SUBMISSION
		
		// This is an example of how to use the crypt function. 
		// The first argument is the password to hash, the second argument is the salt.
		// Notice that the salt is prefixed with $1$. This tells the crypt function
		// to use MD5 hash function to compute the hash. 
		
		String hash = Crypt.crypt("hello","$1$dbCiCUMY");
		
		System.out.print("Hash of hello with salt dbCiCUMY using MD5 crypt: " + hash);
		// END OF SAMPLE CODES
		System.out.println();
		try {
			loadHashes("hash.txt");
			loadDictionary("dict.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		long startTime = System.currentTimeMillis();
		startAttack();
		long endTime = System.currentTimeMillis();
		System.out.println("Took "+(endTime - startTime)+"ms");
	}

	public static void loadHashes(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, "$");
			Hash hash = new Hash(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken());
			hashes.add(hash);
		}
		br.close();
	}
	
	public static void loadDictionary(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null) {
			ArrayList<String> wordList = dictionary.get(line.length());
			if (wordList == null) {
				wordList = new ArrayList<String>();
				dictionary.put(line.length(), wordList);
			}
			wordList.add(line);
		}
		//for (int key : dictionary.keySet())
		//	System.out.println(key+": "+dictionary.get(key).size());
		br.close();
	}
	
	public static void putIntoQueue(BlockingQueue<String> queue, String item) {
		boolean added = false;
		while (!added) {
			try {
				queue.put(item);
				added = true;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public static void startAttack() {
		for (int i=1; i<=PASSWORD_MAX_LENGTH; i++) {
			if (i>=PASSWORD_MIN_LENGTH)
				passwordQ.addAll(dictionary.get(i)); // Process dictionary words first.
		}
		passwordTransformThreadPool.execute(new PasswordTransformThread());
		for (int i=0; i<4; i++) {
			passwordValidationThreadPool.execute(new PasswordValidationThread());
		}
		boolean finished = false;
		while (!finished) {
			if (wordQ.size() <= 0 && !passwordTransformThreadPool.isShutdown()) {
				passwordTransformThreadPool.shutdown();
			}
			if (passwordQ.size() <= 0) {
				//(validatedQ.size() >= 12 ) && 
				finished = true;
				//String output;
				//while ((output = validatedQ.poll()) != null) System.out.println(output);
			}
			System.out.println("Words: "+wordQ.size());
			System.out.println("Passwords: "+passwordQ.size());
			System.out.println("Hashes remaining: "+hashes.size());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		passwordValidationThreadPool.shutdownNow();
		while (!passwordValidationThreadPool.isTerminated());
		System.out.println("Attack finished.");
	}
}
