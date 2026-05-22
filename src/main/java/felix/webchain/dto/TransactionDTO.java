package felix.webchain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDTO {
    private String sender;
    private String receiver;
    private double amount;
}
