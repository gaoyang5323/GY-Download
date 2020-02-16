package com.kakuiwong.service;

import com.kakuiwong.domain.GyHttpRequest;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public interface DownloadService {

    void startDownload(GyHttpRequest request, Long fileSize);

    void deleteDownload(GyHttpRequest request);

    void stopDownload(GyHttpRequest request);
}
