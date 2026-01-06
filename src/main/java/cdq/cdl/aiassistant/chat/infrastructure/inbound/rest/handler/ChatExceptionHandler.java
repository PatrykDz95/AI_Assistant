package cdq.cdl.aiassistant.chat.infrastructure.inbound.rest.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import cdq.cdl.aiassistant.chat.infrastructure.inbound.rest.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ChatExceptionHandler
{
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex)
    {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex)
    {
        log.warn("Invalid JSON request: {}", ex.getMessage());

        Throwable cause = ex.getCause();
        while (cause != null)
        {
            if (cause instanceof IllegalArgumentException)
            {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(cause.getMessage()));
            }
            cause = cause.getCause();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Invalid JSON format in request body"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex)
    {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred"));
    }
}

