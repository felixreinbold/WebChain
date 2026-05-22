package felix.webchain.model;

import felix.webchain.service.BlockChainService;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BlockChain {

    private List<Block> chain;
    private int difficulty;
    private List<Transaction> pendingTransactions;
    private double amount = 10;

    public BlockChain(int difficulty) {
        this.difficulty = difficulty;
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();

    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }
}
