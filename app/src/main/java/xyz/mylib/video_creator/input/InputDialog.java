package xyz.mylib.video_creator.input;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
public class InputDialog extends DialogFragment implements View.OnClickListener {
    private final static String TAG = "InputDialog";
    private EditText mEtContent;
    private String mTitle;
    private String mHint;
    private OnGetContentListener mOnGetContentListener;

    public InputDialog setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public InputDialog setHint(String hint) {
        this.mHint = hint;
        return this;
    }

    public InputDialog setOnGetContentListener(OnGetContentListener l) {
        mOnGetContentListener = l;
        return this;
    }

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_input, container);
        setClickListener(view, R.id.btn_close);
        setClickListener(view, R.id.btn_ok);
        mEtContent = view.findViewById(R.id.et_content);

        if (mTitle != null) {
            ((TextView) view.findViewById(R.id.tv_title)).setText(mTitle);
        }

        mEtContent.setHint(mHint);

        return view;
    }

    private void setClickListener(View view, int id) {
        view.findViewById(id).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close: {
                dismiss();
                break;
            }
            case R.id.btn_ok: {
                String content = mEtContent.getText().toString();
                if (content.trim().length() == 0) {
                    Toast.makeText(getContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                } else {
                    if (mOnGetContentListener != null) {
                        mOnGetContentListener.getContent(mEtContent.getText().toString());
                    }
                    dismiss();
                }
                break;
            }
        }
    }

    public interface OnGetContentListener {
        void getContent(String content);
    }
}
