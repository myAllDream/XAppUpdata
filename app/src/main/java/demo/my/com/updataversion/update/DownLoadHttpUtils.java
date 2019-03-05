package demo.my.com.updataversion.update;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载文件，使用okHttp网络框架
 */
public class DownLoadHttpUtils {
    private static DownLoadHttpUtils mLoadUtils;
    private OkHttpClient okHttpClient;
    private static Context mContext;

    public static DownLoadHttpUtils getInstance(Context context) {
        if (mLoadUtils == null) {
            mLoadUtils = new DownLoadHttpUtils();
        }
        mContext = context;
        return mLoadUtils;
    }

    private DownLoadHttpUtils() {
        okHttpClient = new OkHttpClient();
    }

    /**
     * @param url          下载链接
     * @param destFileName 下载文件名称
     * @param listener     下载监听
     */
    public void download(final String url, final String destFileName, final OnDownloadListener listener) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onDownloadFailed(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    byte[] buf = new byte[2048];
                    int len = 0;
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + mContext.getApplicationInfo().packageName + File.separator);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File file1 = new File(file, destFileName);
                    Log.i("myself", "-----" + file1.getAbsolutePath());
                    long total = response.body().contentLength();
                    fileOutputStream = new FileOutputStream(file1);
                    long sum = 0;
                    while ((len = inputStream.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        if (listener != null) {
                            listener.onDownloading(progress);
                        }
                    }
                    fileOutputStream.flush();
                    if (listener != null) {
                        listener.onDownloadSuccess(file1);
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onDownloadFailed(e);
                    }
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (Exception e) {

                    }
                }
            }
        });
    }
}
