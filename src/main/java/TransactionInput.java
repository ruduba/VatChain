
public class TransactionInput {
	
	
	public String txOutputId;
	public TransactionOutput UTXO; // UTXO -> unspent transaction output
	
	
	public TransactionInput(String txOutputId) {
		this.txOutputId = txOutputId;
	}
}
