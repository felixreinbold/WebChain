package felix.webchain.dto;

import felix.webchain.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockDTO {
    private int index;
    private long timestamp;
    private String previousHash;
    private List<Transaction> transactions;
    private String hash;
    private int nonce;
}