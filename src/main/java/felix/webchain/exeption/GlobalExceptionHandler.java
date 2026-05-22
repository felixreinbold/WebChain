package felix.webchain.exeption;

import felix.webchain.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Behandelt IllegalArgumentException
     * Z.B. bei Transaktionsvalidierung
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                400,
                ex.getMessage(),
                "INVALID_ARGUMENT"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Behandelt RuntimeException
     * Z.B. wenn Wallet nicht gefunden wird
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                400,
                ex.getMessage(),
                "RUNTIME_ERROR"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Behandelt NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponseDTO> handleNullPointerException(
            NullPointerException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                500,
                "Ein interner Fehler ist aufgetreten (NullPointer)",
                "NULL_POINTER_ERROR"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Behandelt IndexOutOfBoundsException
     * Z.B. wenn Block-Index ungültig ist
     */
    @ExceptionHandler(IndexOutOfBoundsException.class)
    public ResponseEntity<ErrorResponseDTO> handleIndexOutOfBoundsException(
            IndexOutOfBoundsException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                404,
                "Das angeforderte Element wurde nicht gefunden",
                "INDEX_OUT_OF_BOUNDS"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Behandelt alle anderen Exception-Typen
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                500,
                "Ein unerwarteter Fehler ist aufgetreten",
                "INTERNAL_SERVER_ERROR"
        );

        // Debug-Info (Optional - in Produktion entfernen)
        ex.printStackTrace();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}