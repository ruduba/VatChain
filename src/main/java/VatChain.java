import java.util.ArrayList;
import com.google.gson.GsonBuilder;

public class VatChain {
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	
	public static void main(String[]args) {

		blockchain.add(new Block("Hi i am the first block", "0"));
		blockchain.add(new Block("Hi i am the second block", blockchain.get(blockchain.size()-1).hash));
		blockchain.add(new Block("Hi i am the thiiird block", blockchain.get(blockchain.size()-1).hash));

		
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println(blockchainJson);
	}
}
