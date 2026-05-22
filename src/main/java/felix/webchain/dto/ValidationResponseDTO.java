package felix.webchain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ValidationResponseDTO {
    private boolean isValid;
    private String message;
    private int blockCount;
    private Integer errorBlock;        // null wenn valid
    private String errorDescription;   // null wenn valid
}