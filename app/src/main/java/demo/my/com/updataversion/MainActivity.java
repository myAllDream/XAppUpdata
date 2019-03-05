package demo.my.com.updataversion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import demo.my.com.updataversion.update.UpdateDialogFragment;

/**
 * app更新代码，适配8.0，只包含下载安装功能，其余自定义
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv).setOnClickListener(v -> UpdateDialogFragment.newInstance(MainActivity.this, "", "", "", false).show(getSupportFragmentManager(), ""));
    }


}
