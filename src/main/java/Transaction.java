import java.security.*;
import java.util.ArrayList;

public class Transaction {
	
	public String transactionId;
	public PublicKey sender;
	public PublicKey reciepient;
	public float value;
	public byte[] signature; //prevent others from spending from our wallet
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; //rough count of number of generated transactions
	
	public Transaction(PublicKey from, PublicKey to,float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	public String calculateHash() {
	
	sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
	return StringUtil.applySha256(
			StringUtil.getStringFromKey(sender) +
			StringUtil.getStringFromKey(reciepient)+
			Float.toString(value)+sequence
			);
				
}
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient);
		signature = StringUtil.applyECDSASig(privateKey, data);
				}
	
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	public boolean processTransaction() {
		if (verifySignature()== false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
		// gather *unspent* transaction inputs
		for(TransactionInput i : inputs) {
			i.UTXO = VatChain.UTXOs.get(i.txOutputId);
		}
		
		if(getInputsValue() < VatChain.minimumTransaction) {
			System.out.println("#Transaction Inputs to small: "+ getInputsValue());
			return false;
		}
		
		float leftOver = getInputsValue() - value;
		transactionId = calculateHash();
		outputs.add(new TransactionOutput(this.reciepient, value, transactionId));
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));
		
		for(TransactionOutput o: outputs) {
			VatChain.UTXOs.put(o.id, o);
		}
		
		for(TransactionInput i: inputs) {
			if(i.UTXO == null) continue;
			VatChain.UTXOs.remove(i.UTXO.id);
		}
		return true;
	}
}
