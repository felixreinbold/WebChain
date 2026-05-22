package felix.webchain.service;

import felix.webchain.dto.StatusDTO;
import felix.webchain.dto.ValidationResponseDTO;
import felix.webchain.model.Block;
import felix.webchain.model.BlockChain;
import felix.webchain.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlockChainService {

    private BlockChain blockChain;
    private static BlockService blockService = new BlockService();
    private WalletService walletService = new WalletService();

    public BlockChainService() {
        this.blockChain = new BlockChain(2);
        createGenesisBlock();
        initializeGenesisTransactions();
    }

    public void createGenesisBlock() {

        List<Transaction> genesisTransactions = new ArrayList<>();
        Block genesisBlock = new Block(0,"0", genesisTransactions);
        blockService.mineBlock(genesisBlock, blockChain.getDifficulty());
        blockChain.getChain().add(genesisBlock);

    }

    public void mineBlock(String minerAddress){

        for (Transaction tx : blockChain.getPendingTransactions()) {
            walletService.executeTransaction(tx.getSender(), tx.getReceiver(), tx.getAmount());
        }

        String prevHash = blockChain.getChain().get(blockChain.getChain().size()-1).getHash();
        Block newBlock = new Block(
                blockChain.getChain().size(),
                prevHash,
                new ArrayList<>(blockChain.getPendingTransactions())
        );
        blockService.mineBlock(newBlock, blockChain.getDifficulty());
        walletService.addReward(minerAddress, blockChain.getAmount());
        blockChain.getChain().add(newBlock);
        clearPendingTransaction();
    }

    public void addPendingTransaction(Transaction tx){

        walletService.signTransaction(tx);

        if (!walletService.validateTransaction(tx)) {
            throw new IllegalArgumentException("Transaktion-Signatur ungültig!");
        }

        if(!walletService.canTransact(tx.getSender(), tx.getAmount())){
            tx.setValidationError("Nicht genug Saldo");
            tx.setVerified(false);
            throw new IllegalArgumentException("Nicht genug Saldo!");
        }


        tx.setVerified(true);

        blockChain.getPendingTransactions().add(tx);
    }

    public void clearPendingTransaction(){
        blockChain.getPendingTransactions().clear();
    }

    public List<Block> getChain(){
        return blockChain.getChain();
    }

    public ValidationResponseDTO isValid(){
        System.out.println("Validiere Blockchain...");


        for(int i = 1; i<getChain().size(); i++){

            Block currentBlock = getChain().get(i);
            Block previousBlock = getChain().get(i-1);

            if(!currentBlock.calculateHash().equals(currentBlock.getHash())){
                System.out.println("Block: "+i+" hat ungültigen Hash");
                return new ValidationResponseDTO(
                        false,
                        "Blockchain ungültig",
                        getChain().size(),
                        i,  // Welcher Block
                        "Block " + i + " hat ungültigen Hash"  // Welcher Fehler
                );

            }


            if(!currentBlock.getPreviousHash().equals(previousBlock.getHash())){
                System.out.println("Block: "+i+" zeigt auf falschen vorherigen Block");
                return new ValidationResponseDTO(
                        false,
                        "Blockchain ungültig",
                        getChain().size(),
                        i,  // Welcher Block
                        "Block " + i + " zeigt auf falschen vorherigen Block"  // Welcher Fehler
                );

            }

            String target = generateTarget(blockChain.getDifficulty());

            if(!currentBlock.getHash().startsWith(target)){
                System.out.println("Block: "+i+" erfüllt nicht die Proof-of-Work Bedingung");
                return new ValidationResponseDTO(
                        false,
                        "Blockchain ungültig",
                        getChain().size(),
                        i,  // Welcher Block
                        "Block " + i + " erfüllt nicht die Proof-of-Work Bedingung"  // Welcher Fehler
                );

            }
        }
        System.out.println("Blockchain ist gültig!");
        return new ValidationResponseDTO(
                true,
                "Blockchain gültig",
                getChain().size(),
                null,
                null
        );

    }

    public int getTotalBlocks(){
        return getChain().size();
    }

    public int getPendingTransactionCount(){

        return blockChain.getPendingTransactions().size();
    }

    public Block getLastBlock(){
        return blockChain.getChain().get(blockChain.getChain().size()-1);
    }

    private String generateTarget(int difficulty){

        String target = "";
        for(int i = 0; i<difficulty; i++){
            target+="0";
        }
        return target;
    }


    public StatusDTO getStatus(){
        ValidationResponseDTO validation = isValid();
        int totalTx = getChain().stream()
                .mapToInt(b -> b.getTransactions().size())
                .sum();

        return new StatusDTO(
                validation.isValid(),
                getTotalBlocks(),
                blockChain.getDifficulty(),
                getLastBlock().getHash(),
                totalTx
        );
    }

    public List<Transaction> getPendingTransactions(){

        return blockChain.getPendingTransactions();
    }


    public void reset(){
        this.blockChain = new BlockChain(2);
        createGenesisBlock();
        System.out.println("Blockchain wurde zurückgesetzt!");
    }

    private void initializeGenesisTransactions(){
        blockChain.getPendingTransactions().add(
                new Transaction("system", "Alice", 100)
        );
        blockChain.getPendingTransactions().add(
                new Transaction("system", "Peter", 100)
        );
        blockChain.getPendingTransactions().add(
                new Transaction("system", "Nina", 100)
        );
        for (Transaction tx : blockChain.getPendingTransactions()) {
            walletService.signTransaction(tx);
            walletService.validateTransaction(tx);
            tx.setVerified(true);
        }
    }

    public WalletService getWalletService(){
        return this.walletService;
    }

    public BlockChain getBlockChain(){
        return this.blockChain;
    }

    public void changeReward(double amount){
        blockChain.setAmount(amount);
    }

}
