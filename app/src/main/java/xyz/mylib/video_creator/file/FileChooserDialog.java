package xyz.mylib.video_creator.file;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.lang.ref.WeakReference;

import xyz.mylib.video_creator.R;
import xyz.mylib.video_creator.adapter.CommonAdapter;

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
    CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    private int selectIndex = -1;

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }

    public FileChooserDialog setSelectType(int selectType) {
        mFileProvider = FileProvider.newInstance(getOldPath(), selectType);
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

    private void saveOldPath(String oldpath) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("creator", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("oldpath", oldpath).apply();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_file_chooser, container);

        setClickListener(view, R.id.btn_close);
        setClickListener(view, R.id.btn_create);
        setClickListener(view, R.id.btn_select);
        setCheckedChangeListner(view, R.id.cb_show);

        rvFile = view.findViewById(R.id.rv_file);

        initData();

        return view;
    }

    public void initData() {

    }

    private void setClickListener(View view, int id) {
        view.findViewById(id).setOnClickListener(this);
    }


    private void setCheckedChangeListner(View view, int id) {
        ((CheckBox) view.findViewById(id)).setOnCheckedChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close: {
                dismiss();
                break;
            }
            case R.id.btn_create: {
                break;
            }
            case R.id.btn_select: {
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    public static interface OnFileSelectedListener {
        void fileSelected(String path);
    }
}
