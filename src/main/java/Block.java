import java.util.ArrayList;
import java.util.Date;

public class Block {
	
	public String hash;
	public String previousHash;
	//private String data;
	public long timeStamp;
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	public int nonce;
	
	public Block(String previousHash) {
		//this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash();
		}
	
	public String calculateHash() {
		String calculatedHash = StringUtil.applySha256(
				previousHash+
				Long.toString(timeStamp)+
				Integer.toString(nonce)+
				merkleRoot);
		
		return calculatedHash;
		}
	
	public void mineBlock(int difficulty) {
		
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDifficultyString(difficulty); // create a string with difficuly * '0'
		while(!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		System.out.println("Block Mined!!: " + hash);
		}
	
	//add a transaction to this block
	public boolean addTransaction(Transaction transaction) {
		//process transaction and check if valid, unless block is genesis block then ignore
		if(transaction == null) return false;
		if((previousHash != "0")) {
			if((transaction.processTransaction()!= true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		transactions.add(transaction);
		System.out.println("Transaction Successfully added to Block");
		return true;
	}
	
	}
