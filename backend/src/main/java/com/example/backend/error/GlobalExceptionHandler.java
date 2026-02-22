package com.example.backend.error;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 422: JSONは読めたが、@Valid の制約違反（必須不足、範囲外など）
     */
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

        return ResponseEntity.unprocessableEntity().body(body); // 422
    }

    /**
     * 400: JSON自体が壊れている / JSON→DTOの変換に失敗（enum不正、日時フォーマット不正など）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {

        // デフォルト（安全側）：変換/形式エラー
        String code = "INVALID_FORMAT";
        String message = "リクエストの形式が不正です。";

        Throwable cause = ex.getCause();

        // JSONの文法が壊れている場合
        if (cause instanceof JsonParseException) {
            code = "MALFORMED_JSON";
            message = "JSONの形式が不正です。";
        }

        // enum/date-time など、型変換に失敗する場合
        if (cause instanceof InvalidFormatException || cause instanceof MismatchedInputException) {
            code = "INVALID_FORMAT";
            message = "リクエストの値の形式が不正です。";
        }

        ApiErrorResponse body = new ApiErrorResponse(
                code,
                message,
                List.of(new ApiErrorResponse.FieldErrorDetail("body", "malformed JSON or invalid value")),
                OffsetDateTime.now()
        );

        return ResponseEntity.badRequest().body(body); // ★ 400
    }

    private ApiErrorResponse.FieldErrorDetail toDetail(FieldError fieldError) {
        String reason = (fieldError.getDefaultMessage() != null)
                ? fieldError.getDefaultMessage()
                : "invalid value";
        return new ApiErrorResponse.FieldErrorDetail(fieldError.getField(), reason);
    }
}