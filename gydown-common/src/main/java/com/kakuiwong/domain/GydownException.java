package com.kakuiwong.domain;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class GydownException extends RuntimeException {

    private Integer code;
    private String message;

    public GydownException() {
    }

    public GydownException(String message) {
        super(message);
        this.message = message;
    }

    public GydownException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
