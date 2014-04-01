import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

// This is a class template for Assignment 2 for the course
// CCE4024/CPE413/CSC409/CZ4024 - Cryptography and Network Security



public class Assignment2 {
	public static int PASSWORD_MAX_LENGTH = 8;
	public static int PASSWORD_MIN_LENGTH = 5;
	
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
			return false;
		}
	}
	public static class PasswordTransformThread extends Thread {
		public String word;
		public PasswordTransformThread(String word) {
			this.word = word;
		}
		public void run() {
			//System.out.println("Running Password Transform Rule.");
			List<String> passwords = new ArrayList<String>();
			passwords.add(this.word);
			//character_substitution_rule(passwords);
			number_prefix_suffix_rule(passwords);
			multiple_words_rule(passwords);
			for (String password : passwords) {
				if (password.length() >= PASSWORD_MIN_LENGTH && 
					password.length() <= PASSWORD_MAX_LENGTH) {
					putIntoQueue(passwordQ, password);
				}
			}
			passwords = null;
			System.gc();
		}
		public void character_substitution_rule(List<String> passwords) {
			/*String temp;
			for (String password : passwords) {
				for (int i=0; i<password.length(); i++) {
					switch(password.charAt(i)) {
						case "a":
							temp = password.replace
					}
				}
			}*/
		}
		public void number_prefix_suffix_rule(List<String> passwords) {
			String temp;
			int pSize = passwords.size();
			for (int i=0; i<pSize; i++) {
				String password = passwords.get(i);
				if (password.matches("(.*)[0-9](.*)")) continue; // Omit the password if numbers are already present.
				for (int j=0; j<100; j++) { // Appends / prepends at most 2 numbers.
					temp = password+j;
					passwords.add(temp);
					temp = j+password;
					passwords.add(temp);
				}
			}
		}
		public void multiple_words_rule(List<String> passwords) {
			String temp = null;
			int pSize = passwords.size();
			
			for (int i=0; i<pSize ; i++) {
				for (int j=0; j<pSize; j++) {
					String s1 = passwords.get(i);
					String s2 = passwords.get(j);
					temp = s1 + s2;
					if (temp.length() >= PASSWORD_MIN_LENGTH && 
							temp.length() <= PASSWORD_MAX_LENGTH) {
						passwords.add(temp);
					}
					if (temp.length() <= PASSWORD_MAX_LENGTH) { //still able to append another word
						//search for additional string to append
						addWord(passwords, temp);
					}
					
				}
			}			
		}
		private static void addWord(List<String> passwords, String word) {
			String temp = null;
			int pSize = passwords.size();
			for (int j=0; j<pSize;j++) {
				temp = word + passwords.get(j);
				if (temp.length() >= PASSWORD_MIN_LENGTH &&
						temp.length() <= PASSWORD_MAX_LENGTH) {
					passwords.add(temp);
				}
				if (temp.length() <= PASSWORD_MAX_LENGTH) {
					addWord(passwords, temp);
				}
			}
			word = temp;
		}
	}
	public static class PasswordValidationThread extends Thread {
		public String password;
		public PasswordValidationThread(String password) {
			this.password = password;
		}
		public void run() {
			//long startTime = System.currentTimeMillis();
			//System.out.println("Running Password Validation.");
			for (Hash hashObject : hashes) {
				String hash = Crypt.crypt(this.password, hashObject.cryptSalt);
				if (hash.equals(hashObject.hashCheck)) {
					System.out.println("Password Validated: "+hashObject.username+":"+this.password);
					putIntoQueue(validatedQ, hashObject.username+":"+this.password);
					hashes.remove(hashObject);
					//break; // Commented because maybe some other users use the same passwords.
				}
			}
			//long endTime = System.currentTimeMillis();
			//System.out.println((endTime - startTime));
		}
	}
	public static ArrayList<String> dictionary = new ArrayList<String>();
	public static ArrayList<Hash> hashes = new ArrayList<Hash>();
	public static BlockingQueue<String> wordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> passwordQ = new LinkedBlockingQueue<String>();
	public static BlockingQueue<String> validatedQ = new LinkedBlockingQueue<String>();
	public static final int nrOfProcessors = Runtime.getRuntime().availableProcessors();
	public static ExecutorService passwordTransformThreadPool = Executors.newFixedThreadPool(1);
	public static ExecutorService passwordValidationThreadPool = Executors.newFixedThreadPool(nrOfProcessors - 1);
	
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
		   dictionary.add(line);
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
	
	public static void startAttack() {
		try {
			wordQ = new LinkedBlockingQueue<String>(dictionary);
			boolean finished = false;
			while (!finished) {
				String word = null;
				String password = null;
				if ((word = wordQ.poll()) != null) {
					passwordTransformThreadPool.execute(new PasswordTransformThread(word));
				}
				if ((password = passwordQ.poll()) != null) {
					//System.out.println(password);
					passwordValidationThreadPool.execute(new PasswordValidationThread(password));
				}
				if ((validatedQ.size() >= 12 )) {
					finished = true;
					String output;
					while ((output = validatedQ.poll()) != null) System.out.println(output);
				}
				System.out.println("Words: "+wordQ.size());
				System.out.println("Passwords: "+passwordQ.size());
			}
		}
		finally {
			passwordTransformThreadPool.shutdownNow();
			passwordValidationThreadPool.shutdownNow();
			while (!passwordTransformThreadPool.isTerminated());
			while (!passwordValidationThreadPool.isTerminated());
			System.out.println("Attack finished.");
		}
	}
}
