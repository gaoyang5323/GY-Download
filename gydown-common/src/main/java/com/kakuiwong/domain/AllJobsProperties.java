package com.kakuiwong.domain;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class AllJobsProperties {
    private String downloadUrl;
    private String downloadPath;
    private Double percentagePoint;
    private Integer status;
    private Double downLoadSpeed;

    public AllJobsProperties() {
    }

    public AllJobsProperties(String downloadUrl, String downloadPath, Double percentagePoint, Integer status, Double downLoadSpeed) {
        this.downloadUrl = downloadUrl;
        this.downloadPath = downloadPath;
        this.percentagePoint = percentagePoint;
        this.status = status;
        this.downLoadSpeed = downLoadSpeed;
    }

    public Double getDownLoadSpeed() {
        return downLoadSpeed;
    }

    public void setDownLoadSpeed(Double downLoadSpeed) {
        this.downLoadSpeed = downLoadSpeed;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public Double getPercentagePoint() {
        return percentagePoint;
    }

    public void setPercentagePoint(Double percentagePoint) {
        this.percentagePoint = percentagePoint;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
