package felix.webchain.model;


import lombok.Getter;

import java.security.*;

@Getter
public class Wallet {

    private String address;
    private double balance;
    private long createdAt;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Wallet(String address, double balance) {
        this.address = address;
        this.createdAt = System.currentTimeMillis();
        this.balance = balance;
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        this.privateKey=keyPair.getPrivate();
        this.publicKey=keyPair.getPublic();
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

}
