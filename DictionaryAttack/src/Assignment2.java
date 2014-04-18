import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class Assignment2 {
	public static int PASSWORD_MAX_LENGTH = 8;
	public static int PASSWORD_MIN_LENGTH = 5;
	public static int validationCounter = 0;
	public static HashMap<Integer, ArrayList<String>> dictionary = new HashMap<Integer, ArrayList<String>>();
	public static BlockingQueue<Hash> hashes = new LinkedBlockingQueue<Hash>();
	public static BlockingQueue<String> dictionaryPasswordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> prefixSuffixPasswordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> combinationPasswordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> leetspeakPasswordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> suffixCombinationQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> suffixCombinationPasswordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> debugQ = new LinkedBlockingQueue<String>();
 	public static int numberOfProcessors = Runtime.getRuntime().availableProcessors();
	public static ExecutorService threadPool = Executors.newFixedThreadPool(numberOfProcessors);
	
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
			try {
				for (int i=PASSWORD_MIN_LENGTH; i<=PASSWORD_MAX_LENGTH; i++) {
					words.addAll(dictionary.get(i));
				}
				for (String word : words) {
					leetspeak_substitution_rule(word);
				}
				//System.out.println("Leetspeak Substitution Done");
				words.clear();
				for (int i=PASSWORD_MIN_LENGTH-2; i<=PASSWORD_MAX_LENGTH-2; i++) {
					words.addAll(dictionary.get(i));
				}
				for (String word : words) {
					number_prefix_suffix_rule(word);
				}
				//System.out.println("Prefix Suffix Done");
				words_combination_rule();
				//System.out.println("Combination Done");
				suffix_combination_rule();
				//System.out.println("Suffix-Combination Done");
			} catch (InterruptedException e) {
				Thread.interrupted();
				return;
			}
		}
		
		private void leetspeak_substitution_rule(String word) throws InterruptedException {
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
			for (String password : words) leetspeakPasswordQ.put(password);
		}
		
		private void number_prefix_suffix_rule(String word) throws InterruptedException {
			String temp;
			if (word.matches("(.*)[0-9](.*)")) return; // Omit the password if numbers are already present.
			for (int j=0; j<100; j++) { // Appends or prepends at most 2 numbers.
				temp = word+j;
				prefixSuffixPasswordQ.put(temp);
				suffixCombinationQ.put(temp);
				temp = j+word;
				prefixSuffixPasswordQ.put(temp);
			}
		}
		
		private void words_combination_rule() throws InterruptedException {
			String temp = null;
			ArrayList<String> words = new ArrayList<String>();
			for (ArrayList<String> list : dictionary.values())
				words.addAll(list);
			int wSize = words.size();
			for (int i=0; i<wSize; i++) {
				for (int j=0; j<wSize; j++) {
					temp = words.get(i) + words.get(j);
					if ((temp.length()>=PASSWORD_MIN_LENGTH)&&(temp.length()<=PASSWORD_MAX_LENGTH)) {
						combinationPasswordQ.put(temp);
						temp = words.get(j) + words.get(i);
						combinationPasswordQ.put(temp);
					}
				}
			}			
		}
		
		private void suffix_combination_rule() throws InterruptedException {
			ArrayList<String> words = new ArrayList<String>();
			String temp = null;
			for (ArrayList<String> list : dictionary.values())
				words.addAll(list);
			String word;
			while(((word=suffixCombinationQ.poll())!=null)||(hashes.size()!=0)) {
				while(suffixCombinationPasswordQ.size()>50000 && hashes.size() > 0) {
					Thread.sleep(3000);
				}
				for(int i=0; i<words.size(); i++) {
					temp = word+words.get(i);
					if((temp.length()>=PASSWORD_MIN_LENGTH)&&(temp.length()<=PASSWORD_MAX_LENGTH)) {
						suffixCombinationPasswordQ.put(temp);
					}
				}
			}
		}
		
	}
	
	public static class PasswordValidationThread extends Thread {
		/**
		 * ValidationMode
		 * 0 - dictionary word
		 * 1 - leetspeak
		 * 2 - suffix/prefix 
		 * 3 - combination
		 * 4 - suffix combination / double transform
		 */
		BlockingQueue<String> passwordQ;
		int validationMode;
		Thread mainThread;
		PasswordValidationThread(int mode, Thread main) {
			this.setMode(mode);
			this.mainThread = main;
		}
		
		private void setMode(int mode) {
			this.validationMode = mode;
			switch(mode) {
			case 0:
				this.passwordQ = dictionaryPasswordQ;
				break;
			case 1:
				this.passwordQ = leetspeakPasswordQ;
				break;
			case 2:
				this.passwordQ = prefixSuffixPasswordQ;
				break;
			case 3:
				this.passwordQ = combinationPasswordQ;
				break;
			case 4:
				this.passwordQ = suffixCombinationPasswordQ;
				break;
			default:
				break;
			}
		}
		
		public void run() {
			//System.out.println("Running validation thread");
			String password;
			boolean finished = false;
			ArrayList<Hash> hashList = new ArrayList<Hash>(hashes);
			while (this.passwordQ.size() == 0);
			while (!finished) {
				if ((password = this.passwordQ.poll()) != null) {
					if (hashList.size() != hashes.size()) {
						if (hashes.size() == 0) {
							finished = true;
							break;
						} else {
							hashList = new ArrayList<Hash>(hashes);
						}
					}
					for (Hash hashObject : hashList) {
						String hash = Crypt.crypt(password, hashObject.cryptSalt);
						if (hash.equals(hashObject.hashCheck)) {
							System.out.println(hashObject.username+":"+password);
							hashes.remove(hashObject);
							break; // Commented because maybe some other users use the same passwords.
						}
					}
					validationCounter++;
					mainThread.interrupt();
				}
				else {
					if (dictionaryPasswordQ.size() > 0) setMode(0);
					else if (leetspeakPasswordQ.size() > 0) setMode(1);
					else if (prefixSuffixPasswordQ.size() > 0) setMode(2);
					else if (combinationPasswordQ.size() > 0) setMode(3);
					else if (suffixCombinationPasswordQ.size() > 0) setMode(4);
					else finished = true;
				}
			}
		}
	}
	
	public static class DebugThread extends Thread {
		public void run() {
			long attackTime = System.currentTimeMillis();
			long startTime = System.currentTimeMillis()-2000;
			while (hashes.size()>0) {
				long stopTime = System.currentTimeMillis();
				System.out.println("Dictionary PasswordQ: "+dictionaryPasswordQ.size());
				System.out.println("Leetspeak PasswordQ: "+leetspeakPasswordQ.size());
				System.out.println("Prefix Suffix PasswordQ: "+prefixSuffixPasswordQ.size());
				System.out.println("Combination PasswordQ: "+combinationPasswordQ.size());
				System.out.println("Suffix Combination PasswordQ: "+suffixCombinationPasswordQ.size());
				System.out.println("Hashes remaining: "+hashes.size());
				System.out.println("Validation Speed (word/sec): "+validationCounter/((stopTime-startTime)/1000));
				System.out.println();
				validationCounter = 0;
				startTime = System.currentTimeMillis();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					Thread.interrupted();
					break;
				}
			}
			long stopTime = System.currentTimeMillis();
			System.out.println("Attack took: "+((float)(stopTime-attackTime)/(1000*60))+"min");
		}
	}
	
	public static void main(String args[]) {
		String hashFilename;
		String dictionaryFilename;
		Thread debug = null;
		if (args.length < 2) {
			System.out.println("Please specify the hash file and dictionary file.");
			System.out.println("\teg. java Assignment2 hash.txt dict.txt");
			System.exit(1);
		} else if (args.length > 2 && args[2].equals("debug")) {
			debug = new DebugThread();
		}
		try {
			hashFilename = args[0];
			dictionaryFilename = args[1];
			loadHashes(hashFilename);
			loadDictionary(dictionaryFilename);
			if (debug != null) debug.start();
			startAttack();
			if (debug != null) { 
				debug.interrupt(); 
				boolean joined = false;
				while(!joined) try { 
					debug.join(); 
					joined=true;
				} catch (InterruptedException e) { 
					Thread.interrupted(); 
				};
			}
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
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
	
	public static void startAttack() {
		for (int i=PASSWORD_MIN_LENGTH; i<=PASSWORD_MAX_LENGTH; i++) {
			dictionaryPasswordQ.addAll(dictionary.get(i)); // Process dictionary words first.
		}
		Thread passwordTransformThread = new PasswordTransformThread();
		passwordTransformThread.start();
		
		try { Thread.sleep(3000); } catch (InterruptedException e1) {Thread.interrupted();}
		if (numberOfProcessors < 4) threadPool = Executors.newFixedThreadPool(4);
		for (int j=0; j<(numberOfProcessors / 4 + 1); j++) {
			threadPool.execute(new PasswordValidationThread(0, Thread.currentThread()));
			threadPool.execute(new PasswordValidationThread(1, Thread.currentThread()));
			threadPool.execute(new PasswordValidationThread(2, Thread.currentThread()));
			threadPool.execute(new PasswordValidationThread(3, Thread.currentThread()));
			threadPool.execute(new PasswordValidationThread(4, Thread.currentThread()));
		}
		
		boolean finished = false;
		while (!finished) {
			try {
				Thread.sleep(60000); // Sleep for 1 minute.
			} catch (InterruptedException e) {
				Thread.interrupted(); // Clear status flag
				if (((dictionaryPasswordQ.size()<=0)&&(prefixSuffixPasswordQ.size()<=0)&&(leetspeakPasswordQ.size()<=0)&&(combinationPasswordQ.size()<=0)&&(suffixCombinationPasswordQ.size()<=0))||
						(hashes.size()==0)) { 
					finished = true;
					break;
				}
			}
		}
		passwordTransformThread.interrupt();
		threadPool.shutdownNow();
	}
}
