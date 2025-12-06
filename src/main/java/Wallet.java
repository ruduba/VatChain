import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
//import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Wallet {
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	//public List<Transaction> txRec = List<Transaction>();
	public Wallet() {
		generateKeyPair();
	}

	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			
			keyGen.initialize(ecSpec, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
			}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// returns balance of UTXO owned by wallet
	public float getBalance() {
		float total = 0;
		for(Map.Entry<String, TransactionOutput> item: VatChain.UTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
			if(UTXO.isMine(publicKey)) {
				UTXOs.put(UTXO.id, UTXO);
				total += UTXO.value;
			}
			
		}
		return total;
	}
	
	// generates and returns a new transaction from this wallet
	public Transaction sendFunds(PublicKey _reciepient, float value) {
		if(getBalance()<value) {
			System.out.println("#Not enough funds to send transaction. Transaction Discarded");
			return null;
		}
		
		// tx ip list
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total =0;
		for(Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) {
			//System.out.println("Not enough balance");
			break;
			}
		}
		
		Transaction newTransaction = new Transaction(publicKey, _reciepient, value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input : inputs) {
			UTXOs.remove(input.txOutputId);
		}
		return newTransaction;
	}
	
	
}
