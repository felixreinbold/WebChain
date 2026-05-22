package felix.webchain.model;

import lombok.Getter;

import java.security.MessageDigest;
import java.util.List;

@Getter
public class Block {

    private final int index;
    private final long timestamp;
    private final String previousHash;
    private final List<Transaction> transactions;
    private String hash="";
    private int nonce;

    public Block(int index, String previousHash, List<Transaction> transactions) {
        this.index = index;
        this.timestamp = System.currentTimeMillis();
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.nonce=0;
        calculateHash();


    }

    public void  setNonce(int nonce){
        this.nonce=nonce;
    }

    public String calculateHash(){

        String transactionString = "";
        for (Transaction tx : transactions) {
            transactionString += tx.toString();
        }
        String input = index + previousHash+timestamp+transactionString+nonce;

        try{

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for(byte b : hashBytes){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length()==1) hexString.append('0');
                hexString.append(hex);
            }

            System.out.println("Hash in Hexadezimal: "+hexString.toString());
            this.hash= hexString.toString();
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Berechnen des Hashs", e);
        }

    }


}
