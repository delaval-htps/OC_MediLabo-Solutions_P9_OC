package com.medilabosolutions.exception;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomErrorResponse {
    private LocalDateTime timeStamp;
    private HttpStatusCode status;
    private List<String> errors;
    private String message;
    private String path;
}