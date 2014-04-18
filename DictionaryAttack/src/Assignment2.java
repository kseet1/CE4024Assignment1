import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	public static int remainingDictionaryPasswords = 6;
	public static int remainingSuffixPrefixPasswords = 8;
	public static int remainingLeetspeakPasswords = 3;
	public static int remainingCombinationPasswords = 2;
	public static int remainingDoubleTransformPasswords = 1;
	public static long validationCounter=0;
	public static HashMap<Integer, ArrayList<String>> dictionary = new HashMap<Integer, ArrayList<String>>();
	public static BlockingQueue<Hash> hashes = new LinkedBlockingQueue<Hash>();
	public static BlockingQueue<String> wordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> passwordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> suffixPasswordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> multiplePasswordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> leetspeakPasswordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> secondTransformQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> secondTransformPasswordQ = new LinkedBlockingQueue<String>();
	public static int numberOfProcessors = Runtime.getRuntime().availableProcessors();
	public static ExecutorService threadPool = Executors.newFixedThreadPool(numberOfProcessors/2 +1);
	
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
			for (int i=PASSWORD_MIN_LENGTH-2; i<=PASSWORD_MAX_LENGTH-2; i++) {
				words.addAll(dictionary.get(i));
			}
			for (String word : words) {
				number_prefix_suffix_rule(word);
			}
			//System.out.println("Suffix Done");
			words.clear();
			for (int i=PASSWORD_MIN_LENGTH; i<=PASSWORD_MAX_LENGTH; i++) {
				words.addAll(dictionary.get(i));
			}
			for (String word : words) {
				character_substitution_rule(word);
			}
			//System.out.println("Subsitutition Done");
			multiple_words_rule();
			//System.out.println("Combination Done");
			second_transform();
			//System.out.println("Second Transform Done");
		}
		private void second_transform() {
			ArrayList<String> words = new ArrayList<String>();
			String temp = null;
			for (ArrayList<String> list : dictionary.values())
				words.addAll(list);
			String word;
			while(((word=secondTransformQ.poll())!=null)||(hashes.size()!=0)) {
				for(int i=0; i<words.size(); i++) {
					while(secondTransformPasswordQ.size()>1500) {
						try {
							if(hashes.size()==0)
								break;
							Thread.sleep(0);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					temp = word+words.get(i);
					if((temp.length()>=PASSWORD_MIN_LENGTH)&&(temp.length()<=PASSWORD_MAX_LENGTH)) {
						putIntoQueue(secondTransformPasswordQ, temp);
					}
				}
			}
		}
		private void character_substitution_rule(String word) {
			/**
			 * i - 1
			 * e - 3
			 * a - 4
			 * s - 5
			 * t - 7
			 * o - 0
			 **/
			String temp = word;
			LinkedHashSet<String> words = new LinkedHashSet<String>();
			if (temp.contains("i"))
				words.add(temp.replace("i", "1"));
			if (temp.contains("e"))
				words.add(temp.replace("e", "3"));
			if (temp.contains("a"))
				words.add(temp.replace("a", "4"));
			if (temp.contains("s"))
				words.add(temp.replace("s", "5"));
			if (temp.contains("t"))
				words.add(temp.replace("t", "7"));
			if (temp.contains("o"))
				words.add(temp.replace("o", "0"));
			if (temp.matches("(.*)[ieasto](.*)"))
				words.add(temp.replace("i", "1")
							  .replace("e", "3")
							  .replace("a", "4")
							  .replace("s", "5")
							  .replace("t", "7")
							  .replace("o", "0"));
			for (String password : words) putIntoQueue(leetspeakPasswordQ, password);
		}
		private void multiple_words_rule() {
			String temp = null;
			ArrayList<String> words = new ArrayList<String>();
			for (ArrayList<String> list : dictionary.values())
				words.addAll(list);
			int wSize = words.size();
			for (int i=0; i<wSize; i++) {
				for (int j=0; j<wSize; j++) {
					temp = words.get(i) + words.get(j);
					if ((temp.length()>=PASSWORD_MIN_LENGTH)&&(temp.length()<=PASSWORD_MAX_LENGTH)) {
						putIntoQueue(multiplePasswordQ,temp);
						temp = words.get(j) + words.get(i);
						putIntoQueue(multiplePasswordQ,temp);
					}
				}
			}			
		}
		private void number_prefix_suffix_rule(String word) {
			String temp;
			if (word.matches("(.*)[0-9](.*)")) return; // Omit the password if numbers are already present.
			for (int j=0; j<100; j++) { // Appends or prepends at most 2 numbers.
				temp = word+j;
				putIntoQueue(suffixPasswordQ, temp);
				putIntoQueue(secondTransformQ, temp);
				temp = j+word;
				putIntoQueue(suffixPasswordQ, temp);
			}
		}
	}
	public static class PasswordValidationThread extends Thread {
		/**
		 * ValidationMode
		 * 0 - normal word
		 * 1 - suffix/prefix 
		 * 2 - leetspeak
		 * 3 - combination
		 * 4 - double transform
		 */
		BlockingQueue<String> passwordQ;
		int validationMode;
		PasswordValidationThread(BlockingQueue<String> passwordQ, int mode) {
			this.passwordQ = passwordQ;
			this.validationMode = mode;
		}
		public void run() {
			//System.out.println("Running validation thread");
			String password;
			boolean finished = false;
			ArrayList<Hash> hashList = new ArrayList<Hash>(hashes);
			while (!finished) {
				if ((password = passwordQ.poll()) != null) {
					for (Hash hashObject : hashList) {
						String hash = Crypt.crypt(password, hashObject.cryptSalt);
						if (hash.equals(hashObject.hashCheck)) {
							System.out.print(hashObject.username+":"+password+"\n");
							hashes.remove(hashObject);
							switch(validationMode) {
							case 0: 
								remainingDictionaryPasswords--;
								break;
							case 1:
								remainingSuffixPrefixPasswords--;
								break;
							case 2:
								remainingLeetspeakPasswords--;
								break;
							case 3:
								remainingCombinationPasswords--;
								break;
							case 4: 
								remainingDoubleTransformPasswords--;
								break;
							default: break;
							}
							break; // Commented because maybe some other users use the same passwords.
						}
					}
					//validationCounter++;
				}
				if (passwordQ.size()<=0) finished = true;
				switch(validationMode) {
				case 0:
					if(remainingDictionaryPasswords==0) finished=true;
					break;
				case 1:
					if(remainingSuffixPrefixPasswords==0) finished=true;
					break;
				case 2:
					if(remainingLeetspeakPasswords==0) finished=true;
					break;
				case 3:
					if(remainingCombinationPasswords==0) finished=true;
					break;
				case 4:
					if(remainingDoubleTransformPasswords==0) finished=true;
					break;
				default: break;
				}
			}
		}
	}
	
	public static void main(String args[]) throws InterruptedException
	{
		String hashFilename;
		String dictionaryFilename;
		try {
			hashFilename = args[0];
			dictionaryFilename = args[1];
			loadHashes(hashFilename);
			loadDictionary(dictionaryFilename);
			//loadHashes("hash.txt");
			//loadDictionary("dict.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		startAttack();
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
	
	public static void startAttack() throws InterruptedException {
		for (int i=1; i<=PASSWORD_MAX_LENGTH; i++) {
			if (i>=PASSWORD_MIN_LENGTH)
				passwordQ.addAll(dictionary.get(i)); // Process dictionary words first.
		}
		threadPool.execute(new PasswordTransformThread());
		
		Thread.sleep(1000);
		threadPool.execute(new PasswordValidationThread(passwordQ,0));
		threadPool.execute(new PasswordValidationThread(suffixPasswordQ,1));
		threadPool.execute(new PasswordValidationThread(leetspeakPasswordQ,2));
		threadPool.execute(new PasswordValidationThread(multiplePasswordQ,3));
		threadPool.execute(new PasswordValidationThread(secondTransformPasswordQ,4));
		//long startTime = System.currentTimeMillis();
		boolean finished = false;
		while (!finished) {
			if (((passwordQ.size()<=0)&&(suffixPasswordQ.size()<=0)&&(leetspeakPasswordQ.size()<=0)&&(multiplePasswordQ.size()<=0)&&(secondTransformPasswordQ.size()<=0))||
					(hashes.size()==0)) { 
				finished = true;
				break;
			}
			// Allocate available threads to process unsolved password types
			if ((suffixPasswordQ.size()!=0)&&(remainingSuffixPrefixPasswords!=0)) 
				threadPool.execute(new PasswordValidationThread(suffixPasswordQ,1));
			else if ((leetspeakPasswordQ.size()!=0)&&(remainingLeetspeakPasswords!=0))
				threadPool.execute(new PasswordValidationThread(leetspeakPasswordQ,2));
			else if ((multiplePasswordQ.size()!=0)&&(remainingCombinationPasswords!=0)) 
				threadPool.execute(new PasswordValidationThread(multiplePasswordQ,3));
			else if ((secondTransformPasswordQ.size()!=0)&&(remainingDoubleTransformPasswords!=0))
				threadPool.execute(new PasswordValidationThread(secondTransformPasswordQ,4));
				
			//startTime = System.currentTimeMillis();
			/*System.out.println("Passwords: "+passwordQ.size());
			System.out.println("Suffix PasswordQ: "+suffixPasswordQ.size());
			System.out.println("Leetspeak PasswordQ: "+leetspeakPasswordQ.size());
			System.out.println("Combinator PasswordQ: "+multiplePasswordQ.size());
			System.out.println("SecondTransform PasswordQ: "+secondTransformPasswordQ.size());
			System.out.println("Hashes remaining: "+hashes.size());
			*/try {
				Thread.sleep(10000);
				//Print the validation speed
				/*long stopTime = System.currentTimeMillis();
				System.out.println("Validation Speed (word/sec): "+validationCounter/((stopTime-startTime)/1000));
				validationCounter = 0;
				*/
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		System.exit(0);
		threadPool.shutdownNow();
		while (!threadPool.isTerminated());
		System.out.println("Attack finished.");
	}
}
