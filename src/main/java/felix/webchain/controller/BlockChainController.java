package felix.webchain.controller;

import felix.webchain.dto.*;
import felix.webchain.model.Block;
import felix.webchain.model.Transaction;
import felix.webchain.model.Wallet;
import felix.webchain.service.BlockChainService;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/blockchain")
public class BlockChainController {

    private final HandlerMapping resourceHandlerMapping;

    @Autowired
    private BlockChainService blockChainService;

    public BlockChainController(@Nullable HandlerMapping resourceHandlerMapping) {
        this.resourceHandlerMapping = resourceHandlerMapping;
    }

    /**
     * Transaktion hinzufügen
     */
    @PostMapping("/transaction")
    public ResponseEntity<?> addTransaction(@RequestBody TransactionDTO dto) {

        // Validierung: Sender
        if (dto.getSender() == null || dto.getSender().trim().isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Sender erforderlich!", "INVALID_SENDER"),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validierung: Receiver
        if (dto.getReceiver() == null || dto.getReceiver().trim().isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Empfänger erforderlich!", "INVALID_RECEIVER"),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validierung: Amount
        if (dto.getAmount() <= 0) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Betrag muss größer als 0 sein!", "INVALID_AMOUNT"),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            Transaction transaction = new Transaction(
                    dto.getSender(),
                    dto.getReceiver(),
                    dto.getAmount()
            );
            blockChainService.addPendingTransaction(transaction);
            return new ResponseEntity<>("Transaktion hinzugefügt!", HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, e.getMessage(), "TRANSACTION_VALIDATION_FAILED"),
                    HttpStatus.BAD_REQUEST
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, e.getMessage(), "TRANSACTION_ERROR"),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Hinzufügen der Transaktion", "INTERNAL_ERROR"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Block minen
     */
    @PostMapping("/mine")
    public ResponseEntity<?> mine(@RequestBody MineDTO dto) {

        // Validierung: Miner Address
        if (dto.getMinerAddress() == null || dto.getMinerAddress().trim().isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Miner-Adresse erforderlich!", "INVALID_MINER_ADDRESS"),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            blockChainService.mineBlock(dto.getMinerAddress());
            return new ResponseEntity<>(blockChainService.getLastBlock(), HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, e.getMessage(), "MINING_VALIDATION_FAILED"),
                    HttpStatus.BAD_REQUEST
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, e.getMessage(), "MINER_WALLET_NOT_FOUND"),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Mining: " + e.getMessage(), "MINING_FAILED"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Alle Blöcke abrufen
     */
    @GetMapping("/blocks")
    public ResponseEntity<?> getBlocks() {
        try {
            List<Block> blocks = blockChainService.getChain();
            return new ResponseEntity<>(blocks, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Abrufen der Blöcke", "INTERNAL_ERROR"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Einen Block abrufen (nach Index)
     */
    @GetMapping("/blocks/{id}")
    public ResponseEntity<?> getBlock(@PathVariable int id) {

        // Validierung: Index
        if (id < 0) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Block-Index kann nicht negativ sein!", "INVALID_BLOCK_INDEX"),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            List<Block> blocks = blockChainService.getChain();

            if (id >= blocks.size()) {
                return new ResponseEntity<>(
                        new ErrorResponseDTO(404, "Block mit Index " + id + " nicht gefunden", "BLOCK_NOT_FOUND"),
                        HttpStatus.NOT_FOUND
                );
            }

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
            return new ResponseEntity<>(
                    new ErrorResponseDTO(404, "Block nicht gefunden", "BLOCK_NOT_FOUND"),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Abrufen des Blocks", "INTERNAL_ERROR"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Blockchain validieren
     */
    @GetMapping("/isValid")
    public ResponseEntity<?> isValid() {
        try {
            ValidationResponseDTO response = blockChainService.isValid();

            if (response.isValid()) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler bei der Validierung", "VALIDATION_ERROR"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Status der Blockchain abrufen
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            return new ResponseEntity<>(blockChainService.getStatus(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Abrufen des Status", "INTERNAL_ERROR"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Ausstehende Transaktionen abrufen
     */
    @GetMapping("/pending-transactions")
    public ResponseEntity<?> getPendingTransactions() {
        try {
            return new ResponseEntity<>(blockChainService.getPendingTransactions(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Abrufen der Transaktionen", "INTERNAL_ERROR"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Blockchain zurücksetzen
     */
    @PostMapping("/reset")
    public ResponseEntity<?> reset() {
        try {
            blockChainService.reset();
            return new ResponseEntity<>("Blockchain wurde zurückgesetzt", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Zurücksetzen der Blockchain", "RESET_FAILED"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Alle Wallets abrufen
     */
    @GetMapping("/wallets")
    public ResponseEntity<?> getAllWallets() {
        try {
            return new ResponseEntity<>(
                    blockChainService.getWalletService().getAllWallets(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Abrufen der Wallets", "INTERNAL_ERROR"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @GetMapping("/wallets/{address}")
    public ResponseEntity<?> getWallet(@PathVariable String address) {

        // Validierung: Address
        if (address == null || address.trim().isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Wallet-Adresse erforderlich!", "INVALID_ADDRESS"),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            Wallet wallet = blockChainService.getWalletService().getAllWalletsHashMap().get(address);

            WalletDTO dto = new WalletDTO(
                    wallet.getAddress(),
                    wallet.getBalance(),
                    wallet.getCreatedAt()
            );

            return new ResponseEntity<>(dto, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(404, "Wallet '" + address + "' nicht gefunden", "WALLET_NOT_FOUND"),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Abrufen des Saldo", "INTERNAL_ERROR"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Neues Wallet erstellen
     */
    @PostMapping("/wallets")
    public ResponseEntity<?> addWallet(@RequestBody String address) {

        // Validierung: Address
        if (address == null || address.trim().isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Wallet-Adresse erforderlich!", "INVALID_ADDRESS"),
                    HttpStatus.BAD_REQUEST
            );
        }
        if(blockChainService.getWalletService().getAllWallets()
                .stream()
                .anyMatch(wallet -> wallet.getAddress().equals(address))){

            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Wallet ist bereits vorhanden. Wähle eine andere Adresse!", "WALLET_ALREADY_EXISTS"),
                    HttpStatus.BAD_REQUEST  // ← Korrekt!
            );
        }

        try {
            blockChainService.getWalletService().addWallet(address);

            Map<String, Wallet> wallets = blockChainService.getWalletService().getAllWalletsHashMap();
            Wallet wallet = wallets.get(address);

            if (wallet == null) {
                return new ResponseEntity<>(
                        new ErrorResponseDTO(500, "Wallet konnte nicht erstellt werden", "WALLET_CREATION_FAILED"),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            WalletDTO dto = new WalletDTO(
                    wallet.getAddress(),
                    wallet.getBalance(),
                    wallet.getCreatedAt()
            );

            return new ResponseEntity<>(dto, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Erstellen des Wallets: " + e.getMessage(), "WALLET_CREATION_FAILED"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Mining-Schwierigkeit ändern
     */
    @PostMapping("/difficulty")
    public ResponseEntity<?> updateDifficulty(@RequestBody DifficultyDTO dto) {

        // Validierung: Difficulty
        if (dto.getDifficulty() <= 0) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Difficulty muss größer als 0 sein!", "INVALID_DIFFICULTY"),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (dto.getDifficulty() > 10) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Difficulty kann maximal 10 sein!", "DIFFICULTY_EXCEEDS_MAX"),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            blockChainService.getBlockChain().setDifficulty(dto.getDifficulty());
            return new ResponseEntity<>(
                    "Mining-Schwierigkeit auf " + dto.getDifficulty() + " gesetzt",
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Aktualisieren der Schwierigkeit", "DIFFICULTY_UPDATE_FAILED"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Mining-Reward ändern
     */
    @PostMapping("/reward")
    public ResponseEntity<?> updateReward(@RequestBody RewardAmountDTO dto) {

        // Validierung: Amount
        if (dto.getAmount() <= 0) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Reward muss größer als 0 sein!", "INVALID_REWARD_AMOUNT"),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (dto.getAmount() > 1000) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(400, "Reward kann maximal 1000 BTC sein!", "REWARD_EXCEEDS_MAX"),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            blockChainService.getBlockChain().setAmount(dto.getAmount());
            return new ResponseEntity<>(
                    "Mining-Reward auf " + dto.getAmount() + " BTC aktualisiert",
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(500, "Fehler beim Aktualisieren des Rewards", "REWARD_UPDATE_FAILED"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}