// This is a class template for Assignment 2 for the course
// CCE4024/CPE413/CSC409/CZ4024 - Cryptography and Network Security



public class Assignment2 {

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
	}
	
}
