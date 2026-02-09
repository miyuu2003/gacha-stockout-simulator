package com.example.backend.error;
import java.time.OffsetDateTime;
import java.util.List;
/**
 * APIエラー応答の共通フォーマット
 */
public class ApiErrorResponse {
    private String message;
    private String code;
    private OffsetDateTime timestamp;
    private List<FieldErrorDetail> details;

    // 各フィールドのエラー詳細情報コンストラクタ
    public ApiErrorResponse(String code, String message, List<FieldErrorDetail> details, OffsetDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }
    public String getCode() {
        return code;
    }
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
    public List<FieldErrorDetail> getDetails() {
        return details;
    }

    // フィールドごとのエラー詳細情報
    public static class FieldErrorDetail {
        private String field;
        private String reason;

        public FieldErrorDetail(String field, String reason) {
            this.field = field;
            this.reason = reason;
        }

        public String getField() {
            return field;
        }
        public String getReason() {
            return reason;
        }
    }
}
