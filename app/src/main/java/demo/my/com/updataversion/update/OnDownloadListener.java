package demo.my.com.updataversion.update;

import java.io.File;

/**
 * 下载监听
 */
public interface OnDownloadListener {
    /**
     * 下载成功之后的文件
     */
    void onDownloadSuccess(File file);

    /**
     * 下载进度
     */
    void onDownloading(int progress);

    /**
     * 下载异常信息
     */

    void onDownloadFailed(Exception e);
}
