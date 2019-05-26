package xyz.mylib.video_creator.file;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import xyz.mylib.video_creator.R;
import xyz.mylib.video_creator.adapter.CommonAdapter;
import xyz.mylib.video_creator.adapter.CommonHolder;
import xyz.mylib.video_creator.input.InputDialog;

/**
 * <pre>
 *     author : yangzhi.ou
 *     e-mail : xxx@xx
 *     time   : 2019/05/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class FileChooserDialog extends DialogFragment implements OnClickListener, CompoundButton.OnCheckedChangeListener {

    private final static String TAG = "FileChooserDialog";

    RecyclerView rvFile;

    FileProvider mFileProvider;
    CheckBox hideFileCheckBox;
    CommonAdapter<FileProvider.FileData> adapter;
    WeakReference<CheckBox> weakCheckBox;
    OnFileSelectedListener mListener;
    TextView mTvCurPath;

    private int selectIndex = -1;
    private int selectType = FileProvider.TYPE_DIR;

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }

    public FileChooserDialog setSelectType(int selectType) {
        this.selectType = selectType;
        return this;
    }

    public FileChooserDialog setOnFileSelectedListener(OnFileSelectedListener listener) {
        mListener = listener;
        return this;
    }

    private String getOldPath() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("creator", Context.MODE_PRIVATE);
        return sharedPreferences.getString("oldpath", null);
    }

    private void saveOldPath(String oldPath) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("creator", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("oldpath", oldPath).apply();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //添加这一行
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_file_chooser, container);

        setClickListener(view, R.id.btn_close);
        setClickListener(view, R.id.btn_create);
        setClickListener(view, R.id.btn_select);
        setCheckedChangeListener(view, R.id.cb_show);
        hideFileCheckBox = view.findViewById(R.id.cb_show);
        rvFile = view.findViewById(R.id.rv_file);
        mTvCurPath = view.findViewById(R.id.tv_cur_path);

        initData();

        return view;
    }

    public void initData() {
        rvFile.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        mFileProvider = FileProvider.newInstance(getOldPath(), selectType);
        adapter = new CommonAdapter<>(getContext(), mFileProvider.list(), R.layout.item_list_file, this::initListItem);
        rvFile.setAdapter(adapter);
        mTvCurPath.setText("当前路径: " + mFileProvider.getCurPath());
    }

    private void initListItem(CommonHolder holder, FileProvider.FileData data, int position) {
        holder.setText(R.id.txt_path, data.name);
        holder.setItemOnClickListener(v -> {
            if (data.name.equals("../")) {
                selectIndex = -1;
                refreshData(mFileProvider.gotoParent());
            } else {
                selectIndex = -1;
                refreshData(mFileProvider.gotoChild(position));
            }
        });
        holder.setText(R.id.txt_info, data.info);
        if (data.isDir) {
            holder.setSrc(R.id.img_file, R.drawable.ic_wenjian);
            holder.setVisible(R.id.img_back, View.VISIBLE);
        } else {
            holder.setSrc(R.id.img_file, R.drawable.ic_file);
            holder.setVisible(R.id.img_back, View.GONE);
        }

        CheckBox checkBox = holder.getView(R.id.checkBox3);

        if (checkBox != null) {
            checkBox.setVisibility(data.selectable ? View.VISIBLE : View.GONE);
            checkBox.setTag(position);
            checkBox.setChecked(selectIndex == position);
            if (selectIndex == position) {
                weakCheckBox = new WeakReference<>(checkBox);
            }
            checkBox.setOnCheckedChangeListener(this);
        }
    }

    private void setClickListener(View view, int id) {
        view.findViewById(id).setOnClickListener(this);
    }

    private void setCheckedChangeListener(View view, int id) {
        ((CheckBox) view.findViewById(id)).setOnCheckedChangeListener(this);
    }

    private void refreshData(List<FileProvider.FileData> list) {
        adapter.replaceAll(list);
        mTvCurPath.setText("当前路径: " + mFileProvider.getCurPath());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close: {
                dismiss();
                break;
            }
            case R.id.btn_create: {
                new InputDialog().setTitle("新建目录").setOnGetContentListener((content) -> {
                    File dir = new File(mFileProvider.getCurPath(), content);
                    if (dir.exists()) {
                        Toast.makeText(getContext(), "文件夹已存在!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (dir.mkdir()) {
                            refreshData(mFileProvider.refresh());
                        } else {
                            Toast.makeText(getContext(), "创建失败!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show(getFragmentManager());
                break;
            }
            case R.id.btn_select: {
                if (this.selectIndex < 0) {
                    Toast.makeText(this.getContext(), "请选择路径", Toast.LENGTH_SHORT).show();
                } else {
                    String realPath = mFileProvider.getItem(selectIndex).realPath;
                    if (mListener != null) {
                        mListener.fileSelected(realPath);
                    }
                    saveOldPath(new File(realPath).getParent());
                    adapter.clear();
                    mFileProvider = null;
                    System.gc();
                    dismiss();
                }

                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (R.id.checkBox3 == buttonView.getId()) {
            if (isChecked) {
                selectIndex = (int) buttonView.getTag();
                if (weakCheckBox != null && weakCheckBox.get() != null) {
                    CheckBox checkBox0 = weakCheckBox.get();
                    if (buttonView != checkBox0) {
                        checkBox0.setChecked(false);
                    }
                }
                weakCheckBox = new WeakReference<>((CheckBox) buttonView);
            } else {
                weakCheckBox.clear();
                weakCheckBox = null;
            }
        } else {
            selectIndex = -1;
            refreshData(mFileProvider.setFilter(!isChecked));
        }
    }


    public interface OnFileSelectedListener {
        void fileSelected(String path);
    }
}
