package demo.my.com.updataversion.update;

import android.content.Context;
import android.content.SharedPreferences;

import demo.my.com.updataversion.MyApp;

public class SharedPrefUtils {

    private static final String SP_FILE_NAME = MyApp.getInstance().getPackageName();
    private static SharedPrefUtils sharedPrefUtils = null;
    private static SharedPreferences sharedPreferences;

    public static synchronized SharedPrefUtils getInstance() {
        if (null == sharedPrefUtils) {
            sharedPrefUtils = new SharedPrefUtils();
        }
        return sharedPrefUtils;
    }

    private SharedPrefUtils() {
        sharedPreferences = MyApp.getInstance().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }

    private void saveSharedPrefData(SharedPreferences.Editor editor) {
        editor.apply();
    }

    public void setApkPath(String path) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("path", path);
        saveSharedPrefData(edit);
    }

    public String getApkPath() {
        return sharedPreferences.getString("path", "");
    }

}
