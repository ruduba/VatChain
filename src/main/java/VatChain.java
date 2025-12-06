import java.util.ArrayList;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Array;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

public class VatChain {
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	public static int difficulty = 3;
	public static Wallet walletA;
	public static Wallet walletB;
	public static float minimumTransaction = 0.1f;
	public static Transaction genesisTransaction;
	public static ArrayList<Transaction> mempool = new ArrayList<>();
	
	
	public static void main(String[]args) {
		
		// setting bc as sec provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		// create new wallets
		walletA = new Wallet();
		walletB = new Wallet();
		Wallet coinbase = new Wallet();
		Scanner sc = new Scanner(System.in);
		//ArrayList <Block> blocks = new ArrayList <Block>();
		
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);
		genesisTransaction.transactionId = "0"; //manually set transaction ID
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId));
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		System.out.println("Creating and Mining Genesis Block...");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		//blockchain.add(genesis);

		//testing

		while(true) {	
		System.out.println("\n\nHello, Welcome to VatChain. Choose the service you want to avail:");
		System.out.println("1. Check Balance of Wallet A and Wallet B\n2. Send funds from Wallet A to B\n3. Receive funds to Wallet A from Wallet B\n4. Mine a new block\n5. View Block Chain\n6. Check Chain validity\n7. Exit");
		System.out.print("Type your option: ");
		int option = sc.nextInt();
		switch(option) {
		case 1:
			System.out.println("\nWallet A's balance is: "+ walletA.getBalance());
			System.out.println("Wallet B's balance is: "+ walletB.getBalance());
			break;
			
		case 2:
			System.out.println("Enter the amount you want to send: ");
			float sendAmnt = sc.nextFloat();
			System.out.println("Wallet A is attempting to send "+ sendAmnt +" coins to Wallet B...");
			Transaction txs = walletA.sendFunds(walletB.publicKey, sendAmnt);
			mempool.add(txs);
			System.out.println("Transaction Added to mempool. Mine a block to confirm");
			break;
			
		case 3:
			System.out.println("Enter the amount you want to request from Wallet B: ");
			float recvAmnt = sc.nextFloat();
			System.out.println("Wallet A is requesting "+ recvAmnt +" coins from Wallet B...");
			Transaction txr = walletB.sendFunds(walletA.publicKey, recvAmnt);
			
			if(txr != null) {
				mempool.add(txr);
				System.out.println("Transaction Added to mempool. Mine a block to confirm");
				
			}
			break;
			
		case 4: 
			Block block = new Block(blockchain.get(blockchain.size()-1).hash);
			for (Transaction t: mempool) {
				block.addTransaction(t);
			}
			mempool.clear();
			addBlock(block);
			System.out.println("Block mined and added to chain");
			break;
		case 5:
			viewBlockChain();
			break;
			
		case 6:
			isChainValid();
			break;
		case 7:
			System.out.println("Thank you for using VatChain");
			System.exit(0);
		}
		}
		
		/*
		System.out.println("Private and Public Keys: ");
		// test the keys
		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		
		// transaction from wA -> wB
		Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
		
		transaction.generateSignature(walletA.privateKey);
		
		System.out.print("Is the signature verified?" );
		System.out.print(transaction.verifySignature());
		*/
		
	}
	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		for(int i =1; i<blockchain.size();i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			
			if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("#Current Hashes NOT EQUAL");
				return false;
			}
			if(!previousBlock.hash.equals(previousBlock.calculateHash())) {
				System.out.println("#Previous Hashes NOT EQUAL");
				return false;
			}
			//checking if hash is solved
			if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("#This Block hasn't been mined");
				return false;
			}
			
			TransactionOutput tempOutput;
			for(int t = 0; t<currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction (" + t + ") is Invalid");
					return false;
				}
				
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are not equal to outputs on Transaction(" + t + ")");
					return false;
				}
				
				for(TransactionInput input: currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.txOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input Transaction(" + t + ") value is INVALID");
						return false;
					}
					
					tempUTXOs.remove(input.txOutputId);
				}
				for(TransactionOutput output : currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if(currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is NOT who it should be");
					return false;
				}
				if(currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction (" + t + ") output 'change' is not the sender");
				}
			}
		}
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
	
	public static void viewBlockChain() {
		System.out.println("\n====== BLOCKCHAIN ======\n");
		for(int i =0; i<blockchain.size(); i++) {
			Block block = blockchain.get(i);
			System.out.println("Block #" + i);
			System.out.println("Hash: " +block.hash);
			System.out.println("Previous Hash" +block.previousHash);
			System.out.println("Nonce: "+block.nonce);
			System.out.println("Timestamp: "+block.timeStamp);
			System.out.println("Transactions: ");
			for(Transaction tx: block.transactions) {
				System.out.println("	Transaction ID: "+tx.transactionId);
				System.out.println("	From: "+StringUtil.getStringFromKey(tx.sender));
				System.out.println("	Amount: "+tx.value);
				System.out.println("	Inputs: ");
				if(tx.inputs==null||tx.inputs.isEmpty()) {
					System.out.println("	GENESIS BLOCK/NO INPUTS");
				}else {
					for(TransactionInput in: tx.inputs) {
						System.out.println("	Input ref ID: "+in.txOutputId);
					}
				}
				
				System.out.println("	Outputs: "+tx.outputs);
				for(TransactionOutput out: tx.outputs) {
					System.out.println("	Output ID");
					System.out.println("	To:"+StringUtil.getStringFromKey(out.reciepient));
					System.out.println("	Value:"+out.value);
				}
				System.out.println();
			}
			System.out.println("________________________________________________________________");
		}
		System.out.println("\n====== END OF BLOCKCHAIN ======\n");
	}
	
}
