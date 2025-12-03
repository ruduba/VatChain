import java.security.PublicKey;


public class TransactionOutput {
	public String id;
	public PublicKey reciepient; // new owner of the coins
	public float value; // no of coins owned
	public String parentTransactionId; //the id of the transaction this o/p was created in
	
	
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
	}
	
	// is the coin mine?
	public boolean isMine(PublicKey publicKey) {
		return(publicKey == reciepient);
	}
}
