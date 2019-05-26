package xyz.mylib.video_creator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xyz.mylib.creator.handler.CreatorExecuteResponseHander;
import xyz.mylib.creator.task.AvcExecuteAsyncTask;
import xyz.mylib.creator.task.GIFExecuteAsyncTask;
import xyz.mylib.video_creator.adapter.CommonAdapter;
import xyz.mylib.video_creator.file.FileChooserDialog;
import xyz.mylib.video_creator.file.FileProvider;
import xyz.mylib.video_creator.loading.LoadingDialog;
import xyz.mylib.video_creator.util.FileUtil;

public class MainActivity extends AppCompatActivity {

    public final static int REQUEST_CODE_TAKE_PICTURE = 1;
    private int selectIndx = -1;
    private ImageView mIvContent;
    private Button btnDelete;
    private CommonAdapter<String> adapter;
    private WeakReference<ImageView> weakImageView;
    private LoadingDialog mLoadingDialog;

    public List<String> getList() {
        List<String> list = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("creator", Context.MODE_PRIVATE);
        Set<String> imgSet = sharedPreferences.getStringSet("imgs", null);
        if (imgSet != null) {
            list.addAll(imgSet);
        }
        return list;
    }

    public void saveList(List<String> list) {
        SharedPreferences sharedPreferences = getSharedPreferences("creator", Context.MODE_PRIVATE);
        sharedPreferences.edit().putStringSet("imgs", new HashSet<>(list)).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_merge_gif).setOnClickListener(this::importImg);
        findViewById(R.id.btn_merge_video).setOnClickListener(this::importImg);

        mLoadingDialog = new LoadingDialog();

        findViewById(R.id.btn_import).setOnClickListener(this::saveImg);
        mIvContent = findViewById(R.id.iv_content);
        btnDelete = findViewById(R.id.btn_delete);

        RecyclerView mRvList = findViewById(R.id.rv_list);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvList.setLayoutManager(manager);

        adapter = new CommonAdapter<>(this, getList(), R.layout.item_list_img, (holder, data, position) -> {
            ImageView ivContent = holder.getView(R.id.iv_content);
            ivContent.setImageURI(Uri.fromFile(new File(data)));
            ivContent.setAlpha(selectIndx == position ? 1f : 0.7f);
            if (selectIndx == position) {
                weakImageView = new WeakReference<>(ivContent);
            }
            holder.itemView.setOnClickListener(v -> {
                selectIndx = position;
                mIvContent.setImageURI(Uri.fromFile(new File(data)));
                if (weakImageView != null && weakImageView.get() != null && weakImageView.get() != ivContent) {
                    weakImageView.get().setAlpha(0.7f);
                    weakImageView = new WeakReference<>(ivContent);
                    ivContent.setAlpha(1f);
                }
            });
        });

        mRvList.setAdapter(adapter);
        if (adapter.getItemCount() > 0) {
            mIvContent.setImageURI(Uri.fromFile(new File(adapter.getItem(0))));
            selectIndx = 0;
            btnDelete.setEnabled(true);
        } else {
            btnDelete.setEnabled(false);
        }

        btnDelete.setOnClickListener((v) -> {
            int num = adapter.getItemCount() - 1;
            String path = adapter.getItem(selectIndx);
            adapter.remove(selectIndx);
            new File(path).delete();
            btnDelete.setEnabled(num > 0);
            if (num > 0) {
                selectIndx = 0;
                mIvContent.setImageURI(Uri.fromFile(new File(adapter.getItem(selectIndx))));
            } else {
                mIvContent.setImageURI(null);
            }
        });

    }


    private void saveImg(View view) {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(data1 -> {
                    Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //系统常量， 启动相机的关键
                    startActivityForResult(openCameraIntent, REQUEST_CODE_TAKE_PICTURE); // 参数常量为自定义的request code, 在取返回结果时有用
                }).start();
    }

    private void importImg(View view) {

        AndPermission.with(this)
                .runtime()
                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(data1 -> {
                    new FileChooserDialog()
                            .setSelectType(FileProvider.TYPE_DIR)
                            .setOnFileSelectedListener((path -> {
                                chooseFile(path, view.getId() == R.id.btn_merge_gif);
                            }))
                            .show(getSupportFragmentManager());

                }).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        saveList(adapter.getData());
    }

    private void chooseFile(String path, boolean isGif) {
        mLoadingDialog.show(getSupportFragmentManager());
        String name = isGif ? "test.gif" : "test.mp4";
        String dpath = getFileStreamPath(name).getPath();
        CreatorExecuteResponseHander handler = new MergyHandler(mLoadingDialog, dpath, path + "/" + name);
        if (isGif) {
            GIFExecuteAsyncTask.execute(new BitmapProvider(adapter, getSize()), 16, handler, dpath);
        } else {
            AvcExecuteAsyncTask.execute(new BitmapProvider(adapter, getSize()), 16, handler, dpath);
        }
    }

    public int[] getSize() {
        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        int widthPixs = metrics.widthPixels;
        int heightPixs = metrics.heightPixels;

        try {
            // 14 <= SDK_INT < 17
            widthPixs = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
            heightPixs = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
        } catch (Exception e) {
        }

        try {
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
            widthPixs = realSize.x;
            heightPixs = realSize.y;
        } catch (Exception e) {
        }

        return new int[]{widthPixs, heightPixs};
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    Bitmap bm = (Bitmap) data.getExtras().get("data");
                    mIvContent.setImageBitmap(bm);
                    String savePath = FileUtil.saveBitmap(bm, this, "i" + adapter.getItemCount() + ".png");
                    if (savePath != null) {
                        adapter.add(savePath);
                        btnDelete.setEnabled(true);
                        selectIndx = adapter.getItemCount() - 1;
                    }

                }
                break;
        }
    }

}
