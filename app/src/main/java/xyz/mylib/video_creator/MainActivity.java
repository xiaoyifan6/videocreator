package xyz.mylib.video_creator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import xyz.mylib.video_creator.file.FileChooserDialog;
import xyz.mylib.video_creator.file.FileProvider;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_import).setOnClickListener(this::importImg);
    }

    private void importImg(View view) {
        new FileChooserDialog()
                .setSelectType(FileProvider.TYPE_FILE)
                .setOnFileSelectedListener(this::chooseFile)
                .show(getSupportFragmentManager());
    }

    private void chooseFile(String path) {
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkPermission(String permission) {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
            return true;
        } else {
//            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
