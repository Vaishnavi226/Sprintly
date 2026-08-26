package com.sprintly.dto;

public class ApiResponse<T> {
    private T data;
    private String message;
    private Integer status;
    private String error;

    public ApiResponse() {}

    public ApiResponse(T data, String message, Integer status, String error) {
        this.data = data;
        this.message = message;
        this.status = status;
        this.error = error;
    }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, null, null);
    }

    public static <T> ApiResponse<T> error(String error, Integer status) {
        return new ApiResponse<>(null, null, status, error);
    }

    public static <T> ApiResponse<T> builder() {
        return new ApiResponse<>();
    }

    public ApiResponse<T> data(T data) { this.data = data; return this; }
    public ApiResponse<T> message(String message) { this.message = message; return this; }
    public ApiResponse<T> status(Integer status) { this.status = status; return this; }
    public ApiResponse<T> error(String error) { this.error = error; return this; }
    public ApiResponse<T> build() { return this; }
}
