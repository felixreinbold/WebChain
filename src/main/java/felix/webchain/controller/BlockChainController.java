package felix.webchain.controller;


import felix.webchain.dto.*;
import felix.webchain.model.Block;
import felix.webchain.model.Transaction;
import felix.webchain.model.Wallet;
import felix.webchain.service.BlockChainService;
import felix.webchain.service.WalletService;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/blockchain")
public class BlockChainController {


    private final HandlerMapping resourceHandlerMapping;
    private BlockChainService blockChainService = new BlockChainService();


    public BlockChainController(@Nullable HandlerMapping resourceHandlerMapping) {
        this.resourceHandlerMapping = resourceHandlerMapping;
    }

    @PostMapping("/transaction")
    public ResponseEntity<?> addTransaction(@RequestBody TransactionDTO dto){
        try {
            Transaction transaction = new Transaction(
                    dto.getSender(),
                    dto.getReceiver(),
                    dto.getAmount()
            );
            blockChainService.addPendingTransaction(transaction);
            return new ResponseEntity<>("Transaktion hinzugefügt!", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/mine")
    public ResponseEntity<?> mine(){

        blockChainService.mineBlock();

        return new ResponseEntity<>(blockChainService.getLastBlock(), HttpStatus.CREATED);

    }

    @GetMapping("/blocks")
    public ResponseEntity<?> getBlocks(){
        List<Block> blocks = blockChainService.getChain();

        return new ResponseEntity<>(blocks, HttpStatus.OK);
    }

    @GetMapping("/blocks/{id}")
    public ResponseEntity<?> getBlocks(@PathVariable int id){
        try {
            List<Block> blocks = blockChainService.getChain();
            Block block = blocks.get(id);

            BlockDTO blockDTO = new BlockDTO(
                    block.getIndex(),
                    block.getTimestamp(),
                    block.getPreviousHash(),
                    block.getTransactions(),
                    block.getHash(),
                    block.getNonce()
            );

            return new ResponseEntity<>(blockDTO, HttpStatus.OK);

        } catch (IndexOutOfBoundsException e) {
            return new ResponseEntity<>("Block nicht gefunden", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/isValid")
        public ResponseEntity<?>isValid() {

        ValidationResponseDTO response = blockChainService.isValid();

        if(response.isValid()){
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(){
        return new ResponseEntity<>(blockChainService.getStatus(), HttpStatus.OK);
    }

    @GetMapping("/pending-transactions")
    public ResponseEntity<?> getPendingTransactions(){
        return new ResponseEntity<>(blockChainService.getPendingTransactions(), HttpStatus.OK);
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(){
        blockChainService.reset();

        return new ResponseEntity<>(
                "Blockchain wurde zurückgesetzt",
                HttpStatus.OK
        );
    }
    @GetMapping("/wallets")
    public ResponseEntity<?> getAllWallets(){
        return new ResponseEntity<>(blockChainService.getWalletService().getAllWallets(), HttpStatus.OK);
    }

    @GetMapping("/wallets/{address}")
    public ResponseEntity<?> getBalance(@PathVariable String address){
        return new ResponseEntity<>(blockChainService.getWalletService().getBalance(address), HttpStatus.OK);
    }

    @PostMapping ("/wallets")
    public ResponseEntity<?> addWallet(@RequestBody String address){


        blockChainService.getWalletService().addWallet(address);

        Map<String, Wallet> wallets = blockChainService.getWalletService().getAllWalletsHashMap();
        Wallet wallet = wallets.get(address);
        WalletDTO dto = new WalletDTO(
                wallet.getAddress(),
                wallet.getBalance(),
                wallet.getCreatedAt()
        );

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping ("/difficulty")
    public ResponseEntity<?> updateDifficulty(@RequestBody DifficultyDTO dto){

        blockChainService.getBlockChain().setDifficulty(dto.getDifficulty());


        return new ResponseEntity<>(dto.getDifficulty(), HttpStatus.CREATED);
    }


}
