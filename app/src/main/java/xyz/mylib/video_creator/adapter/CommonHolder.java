package xyz.mylib.video_creator.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;  //view的集合
    private View mConvertView;  //item的布局
    private int mLayoutId = -1;

    public CommonHolder(View itemView) {
        super(itemView);
        mConvertView = itemView;
        mViews = new SparseArray<>();
    }

    public CommonHolder(View itemView, int layoutId) {
        this(itemView);
        mLayoutId = layoutId;
    }

    //获取item的布局
    public View getItemView() {
        return mConvertView;
    }

    public int getLayoutId() {
        return mLayoutId;
    }

    //初始化控件，通过传进去id来初始化，使用泛型实现传递任何类型
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    //快捷设置TextView的文本
    public CommonHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    public CommonHolder show(int viewId) {
        TextView tv = getView(viewId);
        tv.setVisibility(View.VISIBLE);
        return this;
    }

    public CommonHolder hide(int viewId) {
        TextView tv = getView(viewId);
        tv.setVisibility(View.GONE);
        return this;
    }

    //快捷设置ImageView的图片
    public CommonHolder setSrc(int viewId, int resId) {
        ImageView view = getView(viewId);
        view.setImageResource(resId);
        return this;
    }

    //设置控件的点击事件
    public CommonHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    //设置item的点击事件
    public CommonHolder setItemOnClickListener(View.OnClickListener listener) {
        mConvertView.setOnClickListener(listener);
        return this;
    }

    /**
     * @param viewId
     * @param listener
     * @return
     */
    public CommonHolder setCheckedChanged(int viewId, CompoundButton.OnCheckedChangeListener listener) {
        View view = getView(viewId);
        ((CheckBox) view).setOnCheckedChangeListener(listener);
        return this;
    }

    /**
     * 设置图片背景颜色
     */
    public CommonHolder setTextColor(int viewId, int color) {
        TextView view = getView(viewId);
        if (view != null) {
            view.setTextColor(color);
        }
        return this;
    }

    /**
     * 设置控件是否可见
     */
    public CommonHolder setVisible(int viewId, int visible) {
        View view = getView(viewId);
        view.setVisibility(visible);
        return this;
    }

    /**
     * 设置控件选中
     */
    public CommonHolder setChecked(int viewId, boolean checked) {
        Checkable view = getView(viewId);
        view.setChecked(checked);
        return this;
    }

//
//    public void setItemDataListener(ItemDataListener listener) {
//        itemDataListener = listener;
//    }
//
//    @Override
//    public void onBindViewHolder(final CommonHolder holder, int position) {
//        if (getItemViewType(position) == TYPE_HEADER || getItemViewType(position) == TYPE_PULL_TO_REFRESH_HEADER) {//如果是头部，不做数据填充
//            return;
//        } else if (getItemViewType(position) == TYPE_FOOTER) {
//            return;
//        } else {
//            if (itemDataListener == null) {
//                return;
//            }
//            itemDataListener.setItemData(holder, datas.get(getRealPosition(holder)));
//        }
//    }
//
//    public static interface ItemDataListener<T> {//接口
//        void setItemData(CommonHolder holder, T t);
//    }

}
