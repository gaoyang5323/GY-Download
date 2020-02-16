package com.kakuiwong.enums;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public enum GyStatusEnum {
    START(1, "启动"), STOP(-1, "停止");

    private String name;
    private Integer status;

    GyStatusEnum(Integer status, String name) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }}
