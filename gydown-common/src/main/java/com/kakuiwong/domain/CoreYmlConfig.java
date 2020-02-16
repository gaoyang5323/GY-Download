package com.kakuiwong.domain;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class CoreYmlConfig {
    private Integer port;
    private String downloadPath;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }
}
