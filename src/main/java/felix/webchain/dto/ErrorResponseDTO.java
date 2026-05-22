package felix.webchain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;              // HTTP Status Code (400, 404, 500, etc.)
    private String message;          // Benutzerfreundliche Fehlermeldung
    private String errorCode;        // Interner Error Code (z.B. "INVALID_AMOUNT")
    private long timestamp;          // Zeitstempel des Fehlers
    private String path;             // API-Pfad wo Fehler occurred

    // Konstruktor für einfache Verwendung (ohne Path)
    public ErrorResponseDTO(int status, String message, String errorCode) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    // Konstruktor nur mit Message und Status
    public ErrorResponseDTO(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}