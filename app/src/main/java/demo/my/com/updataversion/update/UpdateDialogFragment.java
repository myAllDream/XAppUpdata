package demo.my.com.updataversion.update;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import demo.my.com.updataversion.R;

public class UpdateDialogFragment extends DialogFragment implements View.OnClickListener {
    public final static int REQUEST_CODE_REQUEST_PERMISSIONS = 111;
    private final String downApkName = "download.apk";
    private int oldProgress;//上一秒的进度值，防止进度闪动
    /**
     * 顶部图片
     */
    private ImageView mIvTop;
    /**
     * 标题
     */
    private TextView mTvTitle;
    /**
     * 版本更新内容
     */
    private TextView mTvUpdateInfo;
    /**
     * 版本更新
     */
    private Button mBtnUpdate;
    /**
     * 后台更新
     */
    private Button mBtnBackgroundUpdate;
    /**
     * 忽略版本
     */
    private TextView mTvIgnore;
    /**
     * 进度条
     */
    private NumberProgressBar mNumberProgressBar;
    /**
     * 底部关闭
     */
    private LinearLayout mLlClose;
    private ImageView mIvClose;

    private String loadUrl;
    private String loadTitle;
    private String loadContent;
    private boolean isForce;
    private static Context mContext;
    private static boolean isFirst = true;//防止多次重复打开安装权限

    /**
     * 获取更新提示
     *
     * @param loadUrl     下载地址
     * @param loadTitle   下载标题
     * @param loadContent 下载内容
     * @param isForce     是否强制下载，true强制下载
     * @return
     */
    public static UpdateDialogFragment newInstance(Context context, String loadUrl, String loadTitle, String loadContent, boolean isForce) {
        UpdateDialogFragment fragment = new UpdateDialogFragment();
        Bundle args = new Bundle();
        mContext = context;
        args.putString("loadUrl", loadUrl);
        args.putString("loadTitle", loadTitle);
        args.putString("loadContent", loadContent);
        args.putBoolean("isForce", isForce);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.XUpdate_Fragment_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.xupdate_dialog_app, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        //顶部图片
        mIvTop = view.findViewById(R.id.iv_top);
        //标题
        mTvTitle = view.findViewById(R.id.tv_title);
        //提示内容
        mTvUpdateInfo = view.findViewById(R.id.tv_update_info);
        //更新按钮
        mBtnUpdate = view.findViewById(R.id.btn_update);
        //后台更新按钮
        mBtnBackgroundUpdate = view.findViewById(R.id.btn_background_update);
        //忽略
        mTvIgnore = view.findViewById(R.id.tv_ignore);
        //进度条
        mNumberProgressBar = view.findViewById(R.id.npb_progress);
        //关闭按钮+线 的整个布局
        mLlClose = view.findViewById(R.id.ll_close);
        //关闭按钮
        mIvClose = view.findViewById(R.id.iv_close);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            loadUrl = bundle.getString("loadUrl");
            loadTitle = bundle.getString("loadTitle");
            loadContent = bundle.getString("loadContent");
            isForce = bundle.getBoolean("isForce");
            initListeners();
        }

        /*if (judgeVersionByPath() > UpdateUtils.getVersionCode(mContext)) {
            //是更新的
            mBtnUpdate.setText("立即安装");
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if ("5.13.11-m27".equals(judgeVersionByPath())) {
            mBtnUpdate.setText("立即安装");
            mBtnUpdate.setVisibility(View.VISIBLE);
            mNumberProgressBar.setVisibility(View.GONE);
        } else {
            mBtnUpdate.setText("更新");
            mBtnUpdate.setVisibility(View.VISIBLE);
            mNumberProgressBar.setVisibility(View.GONE);
        }
    }

    private void initListeners() {
        mBtnUpdate.setOnClickListener(this);
        mBtnBackgroundUpdate.setOnClickListener(this);
        mIvClose.setOnClickListener(this);
        mTvIgnore.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        initDialog();
    }

    private void initDialog() {
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                //如果是强制更新的话，就禁用返回键
                return keyCode == KeyEvent.KEYCODE_BACK && isForce;
            }
        });

        Window window = getDialog().getWindow();
        if (window != null) {
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams lp = window.getAttributes();
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.width = (int) (displayMetrics.widthPixels * 0.8f);
            window.setAttributes(lp);
        }
        mTvTitle.setText("升级提醒");
        mTvUpdateInfo.setText("请尽快升级");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_update:
                if ("立即安装".equals(mBtnUpdate.getText().toString().trim())) {
                    //立即安装
                    File file = new File(SharedPrefUtils.getInstance().getApkPath());
                    InstallApkUtils.installApk(file, mContext);
                } else {
                    //权限判断是否有访问外部存储空间权限
                    int flag = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (flag != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_REQUEST_PERMISSIONS);
                    } else {
                        mBtnUpdate.setVisibility(View.GONE);
                        mNumberProgressBar.setVisibility(View.VISIBLE);
                        //开始下载最新安装包
                        downApk();
                    }
                }

                break;
            case R.id.btn_background_update:
                //点击后台更新按钮

                break;
            case R.id.iv_close:
                //点击关闭按钮
                dismiss();
                break;
            case R.id.tv_ignore:
                //点击忽略按钮

                break;
        }

    }

    private void downApk() {
        String url = "https://imtt.dd.qq.com/16891/6346B1E3AED16CFE2F05CA570BD4DA81.apk?fsname=com.oupeng.max_5.13.11-m27_46.apk&amp;csr=1bbd";
        DownLoadHttpUtils.getInstance(mContext).download(url, downApkName, new OnDownloadListener() {
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
                Log.i("myself", "!!!!!!!!!!!------失败" + e.getMessage());
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
                    if (isFirst) {
                        isFirst = false;
                        InstallApkUtils.installApk(file, mContext);
                    }
                    break;
                case 1:
                    int progress = (int) msg.obj;
                    //为了防止进度条闪动
                    if (progress > oldProgress) {
                        mNumberProgressBar.setProgress(progress);
                        oldProgress = progress;
                    }
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //升级
                downApk();
            } else {
                //需要权限被拒绝了
                Toast.makeText(mContext, "请先手动打开读取存储权限,否则无法安装", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 根据路径判断安装包版本
     */
    public String judgeVersionByPath() {
        if (!TextUtils.isEmpty(SharedPrefUtils.getInstance().getApkPath())) {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageArchiveInfo(SharedPrefUtils.getInstance().getApkPath(), PackageManager.GET_ACTIVITIES);
            //int version = packInfo.versionCode;
            return packInfo.versionName;
        }
        return "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
