import java.util.ArrayList;
import com.google.gson.GsonBuilder;
import java.security.*;
import java.util.Base64;

public class VatChain {
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static int difficulty = 5;
	public static Wallet walletA;
	public static Wallet walletB;
	
	
	public static void main(String[]args) {
		
		// setting bc as sec provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		// create new wallets
		walletA = new Wallet();
		walletB = new Wallet();
		
		System.out.println("Private and Public Keys: ");
		
		// test the keys
		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		
		// transaction from wA -> wB
		Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
		
		transaction.generateSignature(walletA.privateKey);
		
		System.out.print("Is the signature verified?" );
		System.out.print(transaction.verifySignature());
		
		
	}
	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		
		for(int i =1; i<blockchain.size();i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			
			if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("Current Hashes NOT EQUAL");
				return false;
			}
			if(!previousBlock.hash.equals(previousBlock.calculateHash())) {
				System.out.println("Previous Hashes NOT EQUAL");
				return false;
			}
		}
		return true;
	}
	
}
