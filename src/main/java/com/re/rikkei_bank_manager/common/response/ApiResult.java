package com.re.rikkei_bank_manager.common.response;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResult<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResult<T> success(String message, T data) {
        return ApiResult.<T>builder().success(true).message(message).data(data).build();
    }
}

