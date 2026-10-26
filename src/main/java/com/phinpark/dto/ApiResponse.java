package com.phinpark.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
    private int statusCode;
    private String errorDetails;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Success response
    public ApiResponse(boolean success, String message, T data, int statusCode) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    // Error response
    public ApiResponse(boolean success, String message, int statusCode, String errorDetails) {
        this();
        this.success = success;
        this.message = message;
        this.statusCode = statusCode;
        this.errorDetails = errorDetails;
    }

    public static <T> ApiResponse<T> success(String message, T data, int statusCode) {
        return new ApiResponse<>(true, message, data, statusCode);
    }

    public static <T> ApiResponse<T> error(String message, int statusCode, String errorDetails) {
        return new ApiResponse<>(false, message, statusCode, errorDetails);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
}
