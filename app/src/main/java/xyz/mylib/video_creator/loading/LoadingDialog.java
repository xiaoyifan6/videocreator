package xyz.mylib.video_creator.loading;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import xyz.mylib.video_creator.R;

/**
 * <pre>
 *     author : yangzhi.ou
 *     e-mail : xxx@xx
 *     time   : 2019/05/26
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class LoadingDialog extends DialogFragment {
    private String loadingInfo;
    private TextView mTvLoading;

    private final static String TAG = "LoadingDialog";

    public LoadingDialog setLoadingInfo(String loadingInfo) {
        this.loadingInfo = loadingInfo;
        if (mTvLoading != null) {
            mTvLoading.setText(loadingInfo);
        }
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_loading, container);
        mTvLoading = view.findViewById(R.id.tv_loading);
        if (loadingInfo != null) {
            mTvLoading.setText(loadingInfo);
        }
        return view;
    }

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }
}
