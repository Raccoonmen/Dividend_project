package com.dayone.exception;

import com.dayone.type.ErrorCode;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScrapException extends RuntimeException {
    private ErrorCode errorCode;
    private String errorMessage;
    private HttpStatus httpStatus;

    public ScrapException(ErrorCode errorCode, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
        this.httpStatus = httpStatus;
    }
}
