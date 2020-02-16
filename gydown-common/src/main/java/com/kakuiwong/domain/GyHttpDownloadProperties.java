package com.kakuiwong.domain;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class GyHttpDownloadProperties extends GyHttpRequest {
    private Thread thread;

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
