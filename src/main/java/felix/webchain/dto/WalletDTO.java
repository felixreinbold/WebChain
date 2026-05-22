package felix.webchain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WalletDTO {


    private String address;
    private double balance;
    private long createdAt;

}
