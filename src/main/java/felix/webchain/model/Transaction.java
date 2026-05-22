package felix.webchain.model;


import lombok.Getter;

@Getter
public class Transaction {

    private final String sender;
    private final String receiver;
    private final double amount;
    private final long timestamp;
    private String signature;
    private boolean isVerified;
    private String validationError;

    public Transaction(String sender, String receiver, double amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public void setValidationError(String validationError) {
        this.validationError = validationError;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    @Override
    public String toString() {
        return sender + " -> " + receiver + ": " + amount + " BTC";
    }

    public void setSignature(String signatureStr) {
        this.signature=signatureStr;
    }
}
