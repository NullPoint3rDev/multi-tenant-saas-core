package exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoTenantContextException.class)
    public ResponseEntity<?> handleNoTenantContext() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
