package felix.webchain.service;

import felix.webchain.model.Transaction;
import felix.webchain.model.Wallet;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.*;

public class WalletService {

    private Map<String, Wallet> wallets;

    public WalletService(){
        this.wallets = new HashMap<>();
        addWallet("Alice");
        addWallet("Peter");
        addWallet("Nina");
        addWallet("system", 10000.0);
    }

    public void addWallet(String address){
        wallets.put(address, new Wallet(address, 0.0));
    }
    public void addWallet(String address, double balance){
        wallets.put(address, new Wallet(address, balance));
    }

    public boolean canTransact(String sender, double amount){
        if(wallets.get(sender)==null){
            return false;
        }
        if(wallets.get(sender).getBalance()<amount){
            return false;
        }
        return true;
    }

    public void executeTransaction(String sender, String receiver, double amount){
        if(canTransact(sender, amount)){

            Wallet senderWallet = wallets.get(sender);
            Wallet receiverWallet = wallets.get(receiver);

            if(receiverWallet == null) {
                throw new RuntimeException("Wallet '" + receiver + "' existiert nicht!");
            }

            senderWallet.setBalance(senderWallet.getBalance()-amount);
            receiverWallet.setBalance(receiverWallet.getBalance()+amount);
        }else{
            throw new RuntimeException("Transaktion kann nicht durchgeführt werden!");
        }
    }

    public void signTransaction(Transaction transaction){
        Wallet senderWallet = wallets.get(transaction.getSender());
        if(senderWallet==null){
            throw new RuntimeException("Sender-Wallet nicht gefunden!");
        }

        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(senderWallet.getPrivateKey());
            sig.update(transaction.toString().getBytes());

            byte[] signatureBytes = sig.sign();
            String signatureStr = Base64.getEncoder().encodeToString(signatureBytes);
            transaction.setSignature(signatureStr);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean validateTransaction(Transaction tx) {
        Wallet senderWallet = wallets.get(tx.getSender());
        if (senderWallet == null) {
            return false;
        }

        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(senderWallet.getPublicKey());
            sig.update(tx.toString().getBytes());

            byte[] signatureBytes = Base64.getDecoder().decode(tx.getSignature());
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public void addReward(String minerAddress, double amount){
        //hier muss ich noch implementieren dass es eine fest definierten
        // Reward gibt der sich ändern kann am besten sollte er in der BlockChain definiert sein
        Wallet empfänger = wallets.get(minerAddress);

        if(empfänger==null){
            throw new RuntimeException("empfänger nicht gefunden");
        }

        empfänger.setBalance(empfänger.getBalance()+amount);
    }



    public double getBalance(String address){
        Wallet wallet = wallets.get(address);
        if(wallet==null){
            throw new RuntimeException("empfänger nicht gefunden");
        }
        return wallet.getBalance();
    }

    public List<Wallet> getAllWallets(){
        return new ArrayList<>(wallets.values());
    }
    public Map<String, Wallet> getAllWalletsHashMap(){
        return this.wallets;
    }


}
