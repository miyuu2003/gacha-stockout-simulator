package com.example.backend.error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldErrorDetail> details = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(this::toDetail)
        .toList();

        ApiErrorResponse body = new ApiErrorResponse(
            "VALIDATION_ERROR",
            "入力値の検証に失敗しました。",
            details,
            OffsetDateTime.now()
        );

        return ResponseEntity.status(422).body(body);
    }

    // Enum不正や日時フォーマット不正など/JSON→DTO変換時のエラー対応
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        ApiErrorResponse body = new ApiErrorResponse(
            "MALFORMED_REQUEST",
            "リクエストの形式が不正です。",
            List.of(new ApiErrorResponse.FieldErrorDetail("body", "malformed JSON or invalid value")),
            OffsetDateTime.now()
        );

        return ResponseEntity.status(422).body(body);
    }

    private ApiErrorResponse.FieldErrorDetail toDetail(FieldError fieldError) {
        String reason = (fieldError.getDefaultMessage() != null) ? fieldError.getDefaultMessage() : "invalid value";
        return new ApiErrorResponse.FieldErrorDetail(fieldError.getField(), reason);
    }
}
