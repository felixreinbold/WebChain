package felix.webchain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StatusDTO {
    private boolean isValid;
    private int blockCount;
    private int difficulty;
    private String lastBlockHash;
    private int totalTransactions;
}