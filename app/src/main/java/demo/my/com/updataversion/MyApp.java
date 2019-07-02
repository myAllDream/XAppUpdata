package demo.my.com.updataversion;

import android.app.Application;

public class MyApp extends Application {
    private static MyApp application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static MyApp getInstance() {
        return application;
    }

}
