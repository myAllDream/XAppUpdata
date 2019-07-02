package demo.my.com.updataversion.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;

public class InstallApkUtils {

    public static void installApk(File file, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean hasInstallPerssion = context.getPackageManager().canRequestPackageInstalls();
            if (!hasInstallPerssion) {
                //8.0没有安装应用权限，应该提示用户打开应用权限
                startInstallPermissionSettingActivity(context);
                return;
            }
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".updateFileProvider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 开启设置安装未知来源应用权限界面
     *
     * @param context
     */
    public static void startInstallPermissionSettingActivity(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent();
        //获取当前apk包URI，并设置到intent中（这一步设置，可让“未知应用权限设置界面”只显示当前应用的设置项）
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        intent.setData(packageURI);
        //设置不同版本跳转未知应用的动作
        if (Build.VERSION.SDK_INT >= 26) {
            intent.setAction(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        } else {
            intent.setAction(android.provider.Settings.ACTION_SECURITY_SETTINGS);
        }
        context.startActivity(intent);
        Toast.makeText(context, "请先打开安装权限", Toast.LENGTH_SHORT).show();
    }
}
