package demo.my.com.updataversion.update;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import demo.my.com.updataversion.R;

/**
 * 更新提醒Popu
 */
public class UpdateNoticePopu extends PopupWindow implements View.OnClickListener {
    public final static int REQUEST_CODE_REQUEST_PERMISSIONS = 1000;
    private final String downApkName = "ylb.apk";
    private int oldProgress;//上一秒的进度值，防止进度闪动
    private ImageView update_img;
    private LinearLayout updata_ll;
    private TextView updata_negative;
    private TextView updata_positive;
    private NumberProgressBar updata_progress;
    private View updata_space;
    private Activity mContext;
    private String upDateUrl;

    public UpdateNoticePopu(Activity context) {
        super(context);
        this.mContext = context;
        View mView = LayoutInflater.from(context).inflate(R.layout.update_notice_popu_layout, null);
        setContentView(mView);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setClippingEnabled(false);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        initView(mView);
    }

    private void initView(View view) {
        update_img = view.findViewById(R.id.update_img);
        updata_ll = view.findViewById(R.id.updata_ll);
        updata_negative = view.findViewById(R.id.updata_negative);
        updata_positive = view.findViewById(R.id.updata_positive);
        updata_progress = view.findViewById(R.id.updata_progress);
        updata_space = view.findViewById(R.id.updata_space);
        updata_negative.setOnClickListener(this);
        updata_positive.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updata_negative:
                dismiss();
                break;
            case R.id.updata_positive:
                if (TextUtils.isEmpty(upDateUrl)) {
                    Toast.makeText(mContext, "无效下载地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ("立即安装".equals(updata_positive.getText().toString().trim())) {
                    //立即安装
                    File file = new File(SharedPrefUtils.getInstance().getApkPath());
                    InstallApkUtils.installApk(file, mContext);
                } else {
                    //权限判断是否有访问外
                    // 部存储空间权限
                    int flag = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (flag != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_REQUEST_PERMISSIONS);
                    } else {
                        updata_ll.setVisibility(View.GONE);
                        updata_progress.setVisibility(View.VISIBLE);
                        //开始下载最新安装包
                        downApk();
                    }
                }
                break;
        }
    }

    private void downApk() {
        String url = "https://imtt.dd.qq.com/16891/6346B1E3AED16CFE2F05CA570BD4DA81.apk?fsname=com.oupeng.max_5.13.11-m27_46.apk&amp;csr=1bbd";
        DownLoadHttpUtils.getInstance(mContext).download(upDateUrl, downApkName, new OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                Message msg = mHandler.obtainMessage();
                msg.what = 0;
                msg.obj = file;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onDownloading(int progress) {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = progress;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onDownloadFailed(Exception e) {
                Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    File file = (File) msg.obj;
                    InstallApkUtils.installApk(file, mContext);
                    updata_positive.setText("立即安装");
                    updata_ll.setVisibility(View.VISIBLE);
                    updata_progress.setVisibility(View.GONE);
                    break;
                case 1:
                    int progress = (int) msg.obj;
                    //为了防止进度条闪动
                    if (progress > oldProgress) {
                        updata_progress.setProgress(progress);
                        oldProgress = progress;
                    }
                    break;
            }
        }
    };

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //升级
                updata_ll.setVisibility(View.GONE);
                updata_progress.setVisibility(View.VISIBLE);
                downApk();
            } else {
                //需要权限被拒绝了
                Toast.makeText(mContext, "请在设置>应用>权限里面打开读取存储权限,否则无法安装", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setShow(RelativeLayout layout, String upDateUrl, boolean isForce) {
        if (isForce) {
            //强制更新
            updata_negative.setVisibility(View.GONE);
            updata_space.setVisibility(View.GONE);
        } else {
            updata_negative.setVisibility(View.VISIBLE);
            updata_space.setVisibility(View.VISIBLE);
        }
        if (getVersionByPath() != 0 && getVersionByPath() > UpdateUtils.getVersionCode(mContext)) {
            updata_positive.setText("立即安装");
            updata_ll.setVisibility(View.VISIBLE);
            updata_progress.setVisibility(View.GONE);
        } else {
            updata_positive.setText("立即更新");
            updata_ll.setVisibility(View.VISIBLE);
            updata_progress.setVisibility(View.GONE);
        }
        this.upDateUrl = upDateUrl;
        if (!isShowing()) {
            try {
                showAtLocation(layout, Gravity.CENTER, 0, 0);
            } catch (Exception e) {

            }
        }
    }

    /**
     * 根据路径判断安装包版本
     */
    public int getVersionByPath() {
        if (!TextUtils.isEmpty(SharedPrefUtils.getInstance().getApkPath())) {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageArchiveInfo(SharedPrefUtils.getInstance().getApkPath(), PackageManager.GET_ACTIVITIES);
            return packInfo.versionCode;
        }
        return 0;
    }

    @Override
    public void dismiss() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.dismiss();
    }
}
